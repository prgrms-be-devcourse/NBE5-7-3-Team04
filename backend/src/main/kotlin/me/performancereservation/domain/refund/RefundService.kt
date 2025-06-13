package me.performancereservation.domain.refund

import io.github.oshai.kotlinlogging.KotlinLogging
import me.performancereservation.domain.refund.dto.RefundDetailResponse
import me.performancereservation.domain.refund.dto.RefundResponse
import me.performancereservation.domain.refund.dto.UpdateBankInfoRequest
import me.performancereservation.domain.refund.enums.RefundStatus
import me.performancereservation.domain.refund.mapper.RefundDetailMapper
import me.performancereservation.domain.reservation.Reservation
import me.performancereservation.domain.reservation.ReservationRepository
import me.performancereservation.domain.sms.SMSService
import me.performancereservation.global.exception.ErrorCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.stream.Collectors
import kotlin.math.min

private val log = KotlinLogging.logger {}

@Service
class RefundService(
    private val smsService: SMSService,
    private val refundRepository: RefundRepository,
    private val reservationRepository: ReservationRepository,
    private val refundDetailMapper: RefundDetailMapper
) {
    companion object {
        private const val BATCH_SIZE = 1000

        // string -> RefundStatus로 변환. 변환 불가능할 경우 throw exception
        private fun getRefundStatus(refundStatus: String): RefundStatus {
            val status: RefundStatus

            try { // 문자열 쿼리 파라미터를 대문자로 변환하여 RefundStatus 생성 시도
                status = RefundStatus.valueOf(refundStatus.uppercase(Locale.getDefault()))
            } catch (e: IllegalArgumentException) {
                // 유효하지 않은 종류의 refundStatus 문자열이 들어왔을 경우
                throw ErrorCode.INVALID_REFUND_STATUS.domainException("유효하지 않은 종류의 refund status로 생성요청. status: $refundStatus")
            }
            return status
        }
    }

    /** 사용자가 단일 예약에 대해 환불 생성할 때 이용.
     * 예약id를 받고 Refund를 생성하여 저장
     *
     * @param reservation 환불을 생성할 예약. 예약서비스에서 인자를 넣어 호출한다
     */
    fun save(reservation: Reservation) {
        val reservationId = reservation.id
        val userId = reservation.userId

        // 이미 같은 예약ID인 Refund가 존재한다면 예외 던짐 (이미 환불신청된 이력 있음)
        val existingRefund = refundRepository.findRefundByReservationId(reservationId!!)
        if (existingRefund != null) {
            throw ErrorCode.DUPLICATE_REFUND.domainException("이미 환불 신청된 예약입니다. reservationId: $reservationId")
        }

        // PENDING 상태로 환불 생성 후 저장
        val newRefund = Refund(
            id = null,  // id는 null로 두거나 자동 생성
            reservationId = reservationId,
            userId = userId,
            status = RefundStatus.PENDING
        )

        refundRepository.save(newRefund)
    }

    /** 모든 id의 환불 디테일 페이지 조회 */
    @Transactional(readOnly = true)
    fun findAllRefundsDetail(status: String?, pageable: Pageable): Page<RefundDetailResponse> {
        if (status == null) {
            // 쿼리로 [Refund, 예약수량, 시작시간, 회차상태, Performance]의 리스트를 받아옴
            val results = refundRepository.findAllRefundsWithDetails(pageable)
            return refundDetailMapper.toRefundDetailResponsePage(results)
        }

        // string -> RefundStatus로 변환
        val refundStatus = getRefundStatus(status)

        val results = refundRepository.findRefundsDetailByStatus(refundStatus, pageable)
        return refundDetailMapper.toRefundDetailResponsePage(results)
    }

    @Transactional(readOnly = true)
    fun findRefundByUserId(userId: Long, reservationId: Long): RefundResponse {
        val refund = refundRepository.findByUserIdAndReservationId(userId, reservationId)
            ?: throw ErrorCode.REFUND_NOT_FOUND.domainException("유저 ID: $userId")
        return RefundResponse.fromEntity(refund)
    }

    @Transactional(readOnly = true)
    fun getRefundIdByUserId(userId: Long, reservationId: Long): Long? {
        val found = refundRepository.findByUserIdAndReservationId(userId, reservationId)
            ?: throw ErrorCode.REFUND_NOT_FOUND.domainException("유저 ID: $userId")
        return found.id
    }

    /** 입력받은 id의 환불 디테일 페이지 조회 */
    @Transactional(readOnly = true)
    fun findAllRefundsDetailByUserId(userId: Long, pageable: Pageable): Page<RefundDetailResponse> {
        val results = refundRepository.findRefundsDetailByUserId(userId, pageable)
        return refundDetailMapper.toRefundDetailResponsePage(results)
    }

    /** refund id의 환불내역 디테일 조회. 없는 refundId인 경우 return null */
    @Transactional(readOnly = true)
    fun findRefundsDetailByRefundId(refundId: Long): RefundDetailResponse {
        val results = refundRepository.findRefundsDetailByRefundId(refundId)

        // refundId로 조회한 결과가 없는 경우 get(0) 실행하기 전에 검사
        if (results.isEmpty()) {
            throw ErrorCode.REFUND_NOT_FOUND.domainException("존재하지 않는 환불입니다. refundId: $refundId")
        }

        return refundDetailMapper.toRefundDetailResponse(results[0])
    }

    /** 특정 환불 상태로 변경할 때 이용 */
    @Transactional
    fun updateRefundStatus(id: Long, status: String) {
        val foundRefund = refundRepository.findByIdOrNull(id)
            ?: throw ErrorCode.REFUND_NOT_FOUND.domainException(
                    "존재하지 않는 환불입니다. refundId: $id")

        foundRefund.status = getRefundStatus(status)
    }

    /** refund를 CONFIRM 상태로 바꾼다. 예약서비스에서 호출 예정 */
    @Transactional
    fun confirmRefund(id: Long) {
        // id로 먼저 찾아보고 해당하는 Refund가 없다면 throw NO_SUCH_REFUND_ERROR
        val refund = refundRepository.findByIdOrNull(id)
            ?: throw ErrorCode.REFUND_NOT_FOUND.domainException(
                    "존재하지 않는 환불입니다. refundId: $id")

        val reservation = reservationRepository.findByIdOrNull(refund.reservationId)
            ?: throw ErrorCode.RESERVATION_NOT_FOUND.domainException(
                "존재하지 않는 예약입니다. reservationId: ${refund.reservationId}")


        // refund domain에서 상태 업데이트
        refund.confirm()
        reservation.cancelConfirm()

        // TODO 시연시 주석 제거
        // 환불 승인 안내 문자
//        smsService.refundConfirmed(refund);
    }

    /**  계좌, 은행, 입금자명 설정, READY state 설정 */
    @Transactional
    fun updateBankInfo(userId: Long, request: UpdateBankInfoRequest): Refund {
        // 해당 refund 존재하는지 유효성검사
        val refund = refundRepository.findByIdOrNull(request.refundId)
            ?: throw ErrorCode.REFUND_NOT_FOUND.domainException(
                    "존재하지 않는 환불입니다. refundId: " + request.refundId
                )

        // 정보를 변경하려는 환불의 user id가 현재 로그인된 user id와 다를 경우 거부
        if (refund.userId != userId) {
            throw ErrorCode.UNAUTHORIZED_REFUND_UPDATE.domainException("본인의 환불만 변경할 수 있습니다.")
        }

        // 계좌정보 설정
        refund.updateBankInfo(request.account, request.bank, request.depositorName)
        // PENDING -> READY 설정
        refund.ready()

        return refund
    }

    /** 전체 refund 목록 조회 (간단한 내용) */
    @Transactional(readOnly = true)
    fun findAllRefunds(): List<RefundResponse> {
        val foundRefunds = refundRepository.findAll()
        //RefundResponse 내부의 fromEntity 메서드로 각각 변환
        return foundRefunds.map { RefundResponse.fromEntity(it) }
    }
    /** 전체 refund 목록 상태별 조회 (간단한 내용) */
    @Transactional(readOnly = true)
    fun findAllRefundByStatus(status: RefundStatus): List<RefundResponse> {
        val foundRefunds = refundRepository.findRefundByStatus(status)
        return foundRefunds.filterNotNull()
            .map { RefundResponse.fromEntity(it) }
    }

    /**
     * 대량의 예약에 대한 환불을 일괄적으로 생성합니다.
     * 결제승인 상태의 예약들에 대해 환불을 생성하며, 벌크 인서트와 배치 처리를 적용합니다.
     *
     * @param reservationList 환불을 생성할 예약 목록
     */
    @Transactional
    fun saveRefundFromReservationList(reservationList: List<Reservation>?) {
        if (reservationList.isNullOrEmpty()) {
            log.warn { "환불 생성할 예약 목록이 비어있습니다." }
            return
        }

        log.info { "대량 환불 생성 시작: 예약 수={} ${reservationList.size}" }

        // 이미 환불이 생성된 예약 ID 목록 조회
        val existingRefundReservationIds = refundRepository.findRefundByReservationIdIn(
            reservationList.stream()
                .map(Reservation::id)
                .collect(Collectors.toList())
        )

        // 환불 생성할 예약 필터링 (이미 환불이 생성된 예약 제외)
        val refundsToSave = reservationList.stream()
            .filter { reservation: Reservation -> !existingRefundReservationIds.contains(reservation.id) }
            .map { reservation: Reservation ->
                Refund(
                    null,  // id는 null (JPA가 자동 할당)
                    reservation.id!!,
                    reservation.userId,
                    RefundStatus.PENDING
                )
            }
            .collect(Collectors.toList())

        if (refundsToSave.isEmpty()) {
            log.info { "생성할 환불이 없습니다." }
            return
        }

        // 배치 단위로 나누어 저장
        val batches: MutableList<List<Refund>> = ArrayList()
        var i = 0
        while (i < refundsToSave.size) {
            batches.add(
                refundsToSave.subList(
                    i,
                    min((i + BATCH_SIZE).toDouble(), refundsToSave.size.toDouble()).toInt()
                )
            )
            i += BATCH_SIZE
        }

        // 배치 단위로 저장 실행
        for (batch in batches) {
            refundRepository.saveAll(batch)
            log.info { "${"환불 배치 저장 완료: {}개"} ${batch.size}" }
        }

        log.info {
            "${"대량 환불 생성 완료: 총 {}개 중 {}개 생성됨"} ${reservationList.size} ${
                refundsToSave.size}"
        }
    }


}