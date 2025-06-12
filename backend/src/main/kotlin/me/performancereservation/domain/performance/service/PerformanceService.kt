package me.performancereservation.domain.performance.service

import me.performancereservation.domain.bookmark.BookmarkRepository
import me.performancereservation.domain.file.File
import me.performancereservation.domain.file.FileRepository
import me.performancereservation.domain.performance.dto.performance.*
import me.performancereservation.domain.performance.dto.performanceschedule.PerformanceScheduleResponse
import me.performancereservation.domain.performance.entities.Performance
import me.performancereservation.domain.performance.entities.PerformanceSchedule
import me.performancereservation.domain.performance.enums.PerformanceCategory
import me.performancereservation.domain.performance.enums.PerformanceStatus
import me.performancereservation.domain.performance.event.PerformanceCanceledEvent
import me.performancereservation.domain.performance.mapper.PerformanceMapper
import me.performancereservation.domain.performance.mapper.PerformanceScheduleMapper
import me.performancereservation.domain.performance.repository.PerformanceRepository
import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository
import me.performancereservation.domain.ticket.Ticket
import me.performancereservation.domain.ticket.TicketRepository
import me.performancereservation.global.exception.ErrorCode
import me.performancereservation.global.storage.redis.RedisSeatService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors

@Service
class PerformanceService (
    private val performanceRepository: PerformanceRepository,
    private val performanceScheduleRepository: PerformanceScheduleRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val fileRepository: FileRepository,
    private val ticketRepository: TicketRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val redisSeatService: RedisSeatService
){


    /** 공연 등록 요청
     *
     * 관리자에게 공연 등록 요청을 위해 "PENDING" 상태로 공연을 저장
     * @param request
     * @return performanceId
     */
    @Transactional
    fun createPerformance(request: PerformanceCreateRequest, managerId: Long): Long? {
        if (!isRegistrationPeriod(request.startDate, request.endDate)) {
            throw ErrorCode.INVALID_PERFORMANCE_PERIOD.domainException("시작 시간 : " + request.startDate + ", 종료 시간 : " + request.endDate)
        }

        return performanceRepository.save(PerformanceMapper.toEntity(request, managerId)).id
    }

    private fun isRegistrationPeriod(startDate: LocalDateTime, endDate: LocalDateTime): Boolean {
        return startDate.isBefore(endDate)
    }


    /** 공연 수정
     *
     * @param performanceId
     * @param request
     * @return performanceId
     */
    @Transactional
    fun updatePerformance(performanceId: Long, request: PerformanceUpdateRequest, managerId: Long): Long? {
        val performance: Performance = performanceRepository.findByIdOrNull(performanceId) ?:
            throw ErrorCode.PERFORMANCE_NOT_FOUND.domainException("해당하는 공연을 찾을 수 없습니다. id=$performanceId")


        // 권한 검사
        if (!performance.hasPermission(managerId)) {
            throw ErrorCode.PERMISSION_DENIED.domainException("수정 권한이 없습니다. id=$performanceId")
        }

        performance.updateFrom(request)
        return performance.id
    }

    /** 공연 전체 취소
     *
     * 공연의 상태를 취소로 변경하고 연결된 회차 모두 취소 상태로 변경
     * @param performanceId
     */
    @Transactional
    fun cancelPerformance(performanceId: Long, managerId: Long): Long? {
        val performance: Performance = performanceRepository.findByIdOrNull(performanceId) ?:
            throw ErrorCode.PERFORMANCE_NOT_FOUND.domainException("해당하는 공연을 찾을 수 없습니다. id=$performanceId")

        // 권한 검사
        if (!performance.hasPermission(managerId)) {
            throw ErrorCode.PERMISSION_DENIED.domainException(
                "공연을 취소할 권한이 없습니다. performance id=$performanceId, managerId=$managerId"
            )
        }

        // 공연 취소
        performance.cancel()
        // 해당 공연 회차 전체 취소
        performanceScheduleRepository.findByPerformanceIdOrderByStartTimeAsc(performance.id)
            .forEach {obj -> obj.cancel() }

        // 예약 취소 이벤트 호출
        eventPublisher.publishEvent(PerformanceCanceledEvent(performance.id))

        return performance.id
    }

    /** 고객 공연 목록 조회
     *
     * 공연 목록 페이지에서 판매중 상태를 띄우기 위해
     * 회차 테이블에서 남은 좌석수가 1이상이고 공연의 상태가 CONFIRM 인 공연 추출
     *
     * @param pageable
     * @return Page<PerformanceListResponse>
    </PerformanceListResponse> */
    @Transactional(readOnly = true)
    fun getPerformanceList(pageable: Pageable): Page<PerformancePageResponse> {
        // 페이징된 공연 조회
        val performances: Page<Performance> = performanceRepository.findAvailablePerformances(pageable)

        // 페이징된 공연의 파일 id 추출
        val fileIds = getFileIdList(performances)

        // fileId로 조회한 경로 매핑
        val fileUrlMap = getFileUrlMap(fileRepository.findAllById(fileIds))

        // 응답 페이징 반환
        return performances.map { performance: Performance ->
            PerformanceMapper.toListResponse(
                performance,
                fileUrlMap[performance.fileId]
            )
        }
    }


    /** 고객 공연 상세 페이지 조회
     *
     * @param performanceId
     * @return PerformanceDetailResponse
     */
    @Transactional(readOnly = true)
    fun getPerformanceDetail(performanceId: Long, userId: Long?): PerformanceDetailResponse {
        // 공연 조회
        val performance: Performance = performanceRepository.findByIdOrNull(performanceId) ?:
            throw ErrorCode.PERFORMANCE_NOT_FOUND.domainException("해당하는 공연을 찾을 수 없습니다. id=$performanceId")

        // 회차 조회
        val schedules: List<PerformanceSchedule> = performanceScheduleRepository
            .findByPerformanceIdOrderByStartTimeAsc(performance.id)

        var bookmarked = false
        if (userId != null) {
            bookmarked = bookmarkRepository.existsByUserIdAndPerformanceId(userId, performanceId)
        }
        // 파일이 존재하는지
        if (!performance.hasFile()) {
            return PerformanceMapper.toDetailResponse(performance, null, bookmarked, schedules)
        } else {
            // 파일이 존재한다면 조회
            val file: File = fileRepository.findByIdOrNull(performance.fileId) ?:
                throw ErrorCode.FILE_NOT_FOUND.domainException("해당하는 파일을 찾을 수 없습니다. id=" + performance.fileId)

            return PerformanceMapper.toDetailResponse(performance, file.key, bookmarked, schedules)
        }
    }

    /** 공연자 자신의 공연 목록 조회
     *
     * @param pageable
     * @param managerId
     * @return Page<PerformanceManagerListResponse>
    </PerformanceManagerListResponse> */
    @Transactional(readOnly = true)
    fun getPerformanceManagerList(pageable: Pageable, managerId: Long): Page<PerformanceManagerPageResponse> {
        // 공연자의 모든 공연을 페이징 하여 가져옴
        val performances: Page<Performance> = performanceRepository.findByManagerId(managerId, pageable)

        // 페이징된 공연의 파일 id 추출
        val fileIds = getFileIdList(performances)

        // fileId로 조회한 경로 매핑
        val fileUrlMap = getFileUrlMap(fileRepository.findAllById(fileIds))

        return performances.map{ performance: Performance ->
            PerformanceMapper.toManagerListResponse(
                performance,
                fileUrlMap[performance.fileId]
            )
        }
    }

    /** 공연 관리자 공연 상세 페이지 조회
     *
     * @param performanceId
     * @return PerformanceManagerDetailResponse
     */
    @Transactional(readOnly = true)
    fun getPerformanceManagerDetail(performanceId: Long, managerId: Long): PerformanceManagerDetailResponse {
        // 해당 공연 조회
        val performance: Performance = performanceRepository.findByIdOrNull(performanceId) ?:
            throw ErrorCode.PERFORMANCE_NOT_FOUND.domainException("해당하는 공연을 찾을 수 없습니다. id=$performanceId")

        // 공연자의 공연이 맞는지 권한 검사
        if (!performance.hasPermission(managerId)) {
            throw ErrorCode.PERMISSION_DENIED.domainException("performanceId=" + performanceId + "는 managerId=" + managerId + "의 공연이 아닙니다.")
        }

        // 연결된 회차 조회
        val schedules: List<PerformanceSchedule> =
            performanceScheduleRepository.findByPerformanceIdOrderByStartTimeAsc(performance.id)

        // 회차 Response 객체 변환
        val scheduleResponses: List<PerformanceScheduleResponse> = schedules.map{ PerformanceScheduleMapper.toResponse(it) }


        // 파일이 존재하는지
        if (!performance.hasFile()) {
            return PerformanceMapper.toManagerDetailResponse(performance, null, scheduleResponses)
        } else {
            // 연결된 파일 조회
            val file: File = fileRepository.findByIdOrNull(performance.fileId) ?:
                throw ErrorCode.FILE_NOT_FOUND.domainException("해당하는 파일을 찾을 수 없습니다. id=" + performance.fileId)

            return PerformanceMapper.toManagerDetailResponse(performance, file.key, scheduleResponses)
        }
    }

    /** 고객 공연 목록 검색
     *
     * 공연 제목 + 날짜 필터링
     * 공연 장소 + 날짜 필터링
     * @param title
     * @param venue
     * @param start
     * @param end
     * @param pageable
     * @return PerformanceListResponse
     */
    @Transactional(readOnly = true)
    fun searchPerformances(
        title: String?,
        venue: String?,
        start: LocalDateTime?,
        end: LocalDateTime?,
        category: PerformanceCategory?,
        pageable: Pageable
    ): Page<PerformancePageResponse> {
        val performances: Page<Performance> =
            performanceRepository.searchAvailablePerformances(title, venue, start, end, category, pageable)

        val fileIds = getFileIdList(performances)

        val fileUrlMap = getFileUrlMap(fileRepository.findAllById(fileIds))

        return performances.map { performance: Performance ->
            PerformanceMapper.toListResponse(
                performance,
                fileUrlMap[performance.fileId]
            )
        }
    }

    /** 공연자 공연 목록 검색
     *
     * @param managerId 공연자 id
     * @param title 제목
     * @param venue 공연장
     * @param start 필터링 시작 날짜
     * @param end 필터링 종료 날짜
     * @param status 공연 상태 필터링
     * @param pageable 페이징
     * @return PerformanceManagerListResponse
     */
    @Transactional(readOnly = true)
    fun searchManagerPerformances(
        managerId: Long,
        title: String?,
        venue: String?,
        start: LocalDateTime?,
        end: LocalDateTime?,
        status: PerformanceStatus?,
        pageable: Pageable
    ): Page<PerformanceManagerPageResponse> {
        val performances: Page<Performance> =
            performanceRepository.searchManagerPerformances(managerId, status, title, venue, start, end, pageable)

        val fileIds = getFileIdList(performances)

        val fileUrlMap = getFileUrlMap(fileRepository.findAllById(fileIds))

        return performances.map { performance: Performance ->
            PerformanceMapper.toManagerListResponse(
                performance,
                fileUrlMap[performance.fileId]
            )
        }
    }

    /** 매일 00시 정각에 스케줄러에 의해 실행되는 공연 종료 처리 메서드
     *
     */
    @Transactional
    fun completeEndedPerformances() {
        // 공연 완료 상태로 변경
        val now = LocalDateTime.now()
        val endedPerformances: List<Performance> =
            performanceRepository.findByEndDateBeforeAndStatus(now, PerformanceStatus.CONFIRMED)

        // 레디스 회차별 좌석 정보 제거
        endedPerformances.forEach(Consumer<Performance> { performance: Performance ->
            performance.completePerformance()
            // 티켓 만료 처리
            val tickets: List<Ticket> = ticketRepository.findAllByPerformanceId(performance.id)

            tickets.forEach(Consumer { obj: Ticket -> obj.expire() })

            // 레디스 좌석 정보 제거
            val schedules: List<PerformanceSchedule> =
                performanceScheduleRepository.findByPerformanceIdOrderByStartTimeAsc(performance.id)
            schedules.forEach(Consumer<PerformanceSchedule> { schedule: PerformanceSchedule ->
                redisSeatService.deleteSeatStock(schedule.id)
            })
        })
    }

    // 파일 Url 맵 변환 메서드
    private fun getFileUrlMap(files: List<File>): Map<Long, String?> {
        return files.stream()
            .collect(
                Collectors.toMap(
                    { obj: File -> obj.id },
                    { obj: File -> obj.key })
            )
    }

    // 파일 id 리스트 변환 메서드
    private fun getFileIdList(performances: Page<Performance>): List<Long> {
        return performances.content.stream()
            .map<Long>(Performance::fileId)
            .filter { obj: Long? -> Objects.nonNull(obj) }
            .toList()
    }
}
