package me.performancereservation.domain.reservation.service

import me.performancereservation.domain.file.FileRepository
import me.performancereservation.domain.performance.model.SchedulePerformanceInfo
import me.performancereservation.domain.performance.repository.PerformanceRepository
import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository
import me.performancereservation.domain.reservation.ReservationRepository
import me.performancereservation.domain.reservation.dto.ReservationDetailResponse
import me.performancereservation.domain.reservation.dto.ReservationPageResponse
import me.performancereservation.domain.reservation.mapper.ReservationMapper
import me.performancereservation.global.exception.ErrorCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReservationQueryService(
    private val reservationRepository: ReservationRepository,
    private val performanceScheduleRepository: PerformanceScheduleRepository,
    private val performanceRepository: PerformanceRepository,
    private val fileRepository: FileRepository,
    private val reservationMapper: ReservationMapper
) {
    /**
     * 유저 id로 본인의 예약 정보를 전부 조회
     *
     * @param userId 유저 ID
     * @return 페이지네이션된 ReservationListResponse
     */
    @Transactional(readOnly = true)
    fun getAllByUserId(userId: Long, pageable: Pageable): Page<ReservationPageResponse> {
        // 본인의 모든 예약 목록 조회
        val reservations = reservationRepository.findAllByUserId(userId, pageable)

        // 공연 회차 ID 리스트 수집
        val scheduleIds = reservations.content.map { it.scheduleId }


        // in절로 SchedulePerformanceInfo 리스트 조회
        val scheduleInfos = performanceScheduleRepository.findAllSchedulePerformanceInfoByScheduleIds(scheduleIds)

        val infoMap = scheduleInfos.associateBy { it.scheduleId }


        // Page<ReservationListResponseDto>로 매핑
        return reservations.map { reservation ->
            val scheduleInfo = infoMap[reservation.scheduleId]
                ?: throw ErrorCode.PERFORMANCE_SCHEDULE_NOT_FOUND.domainException("공연 회차 ID: ${reservation.scheduleId}")

            reservationMapper.toListResponseDto(reservation, scheduleInfo)
        }
    }

    /**
     * 예약 id로 본인의 상세 예약 정보 조회
     *
     * @param reservationId 예약 ID
     * @param userId 로그인한 사용자의 ID
     * @return ReservationResponse
     */
    @Transactional(readOnly = true)
    fun getByReservationId(reservationId: Long, userId: Long): ReservationDetailResponse {
        // 본인의 예약 조회
        val reservation = reservationRepository.findByIdOrNull(reservationId)
            ?: throw ErrorCode.RESERVATION_NOT_FOUND.domainException("예약 ID: $reservationId")

        // 본인의 예약이 아니면 예외
        if (reservation.userId != userId) {
            throw ErrorCode.PERMISSION_DENIED.serviceException("해당 예약에 접근할 수 없습니다.")
        }

        // 공연 조회(메타 데이터 용)
        val performance = performanceRepository.findByIdOrNull(reservation.performanceId)
            ?: throw ErrorCode.PERFORMANCE_NOT_FOUND.domainException(
                "공연 ID: " + reservation.performanceId
            )

        val file = fileRepository.findByIdOrNull(performance.fileId!!)
            ?: throw ErrorCode.FILE_NOT_FOUND.domainException("파일 ID: ${performance.fileId}")

        val fileUrl = file.key

        // SchedulePerformanceInfo 조회
        val scheduleInfo: SchedulePerformanceInfo =
            performanceScheduleRepository.findSchedulePerformanceInfoByScheduleId(reservation.scheduleId)
                ?: throw ErrorCode.PERFORMANCE_SCHEDULE_NOT_FOUND.domainException(
                    "공연 회차 ID: " + reservation.scheduleId
                )


        // ReservationResponseDto 매핑
        return reservationMapper.toDetailResponseDto(reservation, scheduleInfo, performance, fileUrl!!)
    }
}
