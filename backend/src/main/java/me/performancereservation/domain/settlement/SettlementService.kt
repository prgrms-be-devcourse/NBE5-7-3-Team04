package me.performancereservation.domain.settlement

import me.performancereservation.domain.performance.entities.Performance
import me.performancereservation.domain.performance.entities.PerformanceSchedule
import me.performancereservation.domain.performance.repository.PerformanceRepository
import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository
import me.performancereservation.domain.performance.service.PerformanceService
import me.performancereservation.domain.settlement.dto.SettlementRequest
import me.performancereservation.domain.settlement.dto.SettlementResponse
import me.performancereservation.domain.settlement.dto.SettlementResponse.Companion.fromEntity
import me.performancereservation.domain.settlement.dto.SettlementUpdateRequest
import me.performancereservation.domain.settlement.dto.SettlementUpdateResponse
import me.performancereservation.domain.settlement.dto.SettlementUpdateResponse.Companion.fromSettlement
import me.performancereservation.domain.settlement.enums.SettlementStatus
import me.performancereservation.domain.sms.SMSService
import me.performancereservation.global.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class SettlementService(
    private val smsService: SMSService,
    private val settlementRepository: SettlementRepository,
    private val performanceRepository: PerformanceRepository,
    private val performanceScheduleRepository: PerformanceScheduleRepository,
    private val performanceService: PerformanceService
) {

    private val log = LoggerFactory.getLogger(SettlementService::class.java)

    @Transactional
    fun createSettlement(request: SettlementRequest): Long? {
        // 공연 정보 조회
        val performance = performanceRepository.findById(request.performanceId)
            .orElseThrow {
                ErrorCode.PERFORMANCE_NOT_FOUND.domainException(
                    "존재하지 않는 공연입니다."
                )
            }

        // 공연 스케줄 조회
        var schedules = performanceScheduleRepository.findByPerformanceIdOrderByStartTimeAsc(performance.id)
        log.info("불러온 스케줄 리스트: {}개", schedules?.size)
        if (schedules != null) {
            for (schedule in schedules) {
                log.info(
                    "schedule: id={}, startTime={}, endTime={}",
                    schedule?.id,
                    schedule?.startTime,
                    schedule?.endTime
                )
            }
        }
        if (schedules == null) {
            schedules = listOf() // null이면 빈 리스트로 처리
        }

        // 가장 늦은 공연 날짜 확인 (스케쥴이 없으면 latestSchedule도 null)
        val latestSchedule = schedules.stream()
            .filter { obj: PerformanceSchedule? -> Objects.nonNull(obj) }
            .map { obj: PerformanceSchedule -> obj.startTime }
            .filter { obj: LocalDateTime? -> Objects.nonNull(obj) }
            .max { obj: LocalDateTime?, other: LocalDateTime? -> obj!!.compareTo(other) }
            .orElse(null)

        // 정산 신청 가능 날짜 체크 (스케쥴이 없으면 바로 예외)
        if (latestSchedule == null) {
            throw ErrorCode.PERFORMANCE_NOT_FOUND.domainException("공연 스케줄이 존재하지 않습니다.")
        }
        if (latestSchedule.plusDays(7).isAfter(LocalDateTime.now())) {
            throw ErrorCode.INVALID_SETTLEMENT_REQUEST.domainException("공연 종료 후 7일이 지나야 정산 신청이 가능합니다.")
        }

        // 총 정산 금액 계산
        val totalAmount = calculateTotalAmount(schedules, performance)

        // Settlement 객체 생성
        // settledAt은 아직 값 설정하지 않고 나중에 confirm 할 때 설정
        val settlement = Settlement(
            null,
            request.performanceId,
            totalAmount,
            request.account,
            request.bank,
            SettlementStatus.PENDING
        )

        return settlementRepository.save(settlement).id
    }

    private fun calculateTotalAmount(schedules: List<PerformanceSchedule>, performance: Performance): Int {
        val price = performance.price
        val totalSeats = performance.totalSeats

        log.info("정산금액 계산 ======= 가격 {} 좌석수 {}", price, totalSeats)
        log.info("schedules = {}", schedules)

        // 스케쥴 리스트로 총 정산금액 누적 계산
        return schedules.stream()
            .filter { schedule: PerformanceSchedule -> !schedule.canceled }  // 취소된 스케쥴은 계산하지 않음
            .mapToInt { schedule: PerformanceSchedule -> price * (totalSeats - schedule.remainingSeats) }
            .sum()
    }

    /** PENDING 상태 정산의 은행, 계좌정보 수정 */
    @Transactional
    fun updateSettlement(request: SettlementUpdateRequest): SettlementUpdateResponse {
        log.info("[editSettlement Service] 요청: {}", request)
        val settlement = settlementRepository.findById(request.settlementId)
            .orElseThrow {
                ErrorCode.SETTLEMENT_NOT_FOUND.domainException(
                    "존재하지 않는 정산입니다."
                )
            }

        // 승인된 정산은 정보를 수정할 수 없음
        if (settlement.status == SettlementStatus.CONFIRMED) {
            throw ErrorCode.INVALID_SETTLEMENT_REQUEST.domainException("이미 승인된 정산은 정보를 수정할 수 없습니다.")
        }

        val updatedSettlement = settlement.updateBankInfo(request.bank, request.account)
        return fromSettlement(updatedSettlement)
    }

    @Transactional
    fun findSettlementIdByPerformanceId(performanceId: Long): Long? {
        val settlements = settlementRepository.findSettlementByPerformanceId(performanceId)
        val latest = settlements.stream()
            .max { s1: Settlement?, s2: Settlement? -> s1!!.createdAt.compareTo(s2!!.createdAt) }
            .orElse(null)

        val settlementId = latest?.id

        log.info("공연ID: {}, SettlementID: {}", performanceId, settlementId)

        return settlementId
    }

    @Transactional
    fun confirmSettlement(settlementId: Long): SettlementResponse {
        // 정산 객체 조회
        val settlement = settlementRepository.findById(settlementId)
            .orElseThrow {
                ErrorCode.SETTLEMENT_NOT_FOUND.domainException(
                    "존재하지 않는 정산입니다."
                )
            }

        // 공연 정보 조회
        val performance = performanceRepository.findById(settlement.performanceId)
            .orElseThrow {
                ErrorCode.PERFORMANCE_NOT_FOUND.domainException(
                    "존재하지 않는 공연입니다."
                )
            }

        // 정산 상태 변경 및 완료 시간 설정
        settlement.confirm()

        // TODO 시연시 주석 제거
        // 정산 완료 안내 문자
//        smsService.settlementsConfirmed(settlement, performance);

        // SettlementResponse 생성 및 반환
        return fromEntity(settlement, performance.title)
    }

    @Transactional(readOnly = true)
    fun findAllSettlementsWithUserId(userId: Long, pageable: Pageable): Page<SettlementResponse?> {
        return settlementRepository.findAllSettlementsWithUserId(userId, pageable)
    }

    @Transactional(readOnly = true)
    fun findAllSettlementsByStatus(status: String?, pageable: Pageable): Page<SettlementResponse?> {
        if (status == null) {
            return settlementRepository.findAllSettlements(pageable)
        }

        val settlementStatus = getSettlementStatus(status)
        return settlementRepository.findAllSettlementsByStatus(settlementStatus, pageable)
    }


    // string -> settlementStatus로 변환. 변환 불가능할 경우 throw exception
    private fun getSettlementStatus(settlementStatus: String): SettlementStatus {
        val status: SettlementStatus

        try { // 문자열 쿼리 파라미터를 대문자로 변환하여 settlementStatus 생성 시도
            status = SettlementStatus.valueOf(settlementStatus.uppercase(Locale.getDefault()))
        } catch (e: IllegalArgumentException) {
            // 유효하지 않은 종류의 settlementStatus 문자열이 들어왔을 경우
            throw ErrorCode.INVALID_SETTLEMENT_STATUS.domainException("유효하지 않은 종류의 settlement status로 생성 요청하였습니다. status: $settlementStatus")
        }
        return status
    }
}
