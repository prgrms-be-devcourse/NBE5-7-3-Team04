package me.performancereservation.domain.sms

import io.github.oshai.kotlinlogging.KotlinLogging
import lombok.extern.slf4j.Slf4j
import me.performancereservation.domain.performance.entities.Performance
import me.performancereservation.domain.performance.repository.PerformanceRepository
import me.performancereservation.domain.refund.Refund
import me.performancereservation.domain.reservation.Reservation
import me.performancereservation.domain.reservation.ReservationRepository
import me.performancereservation.domain.settlement.Settlement
import me.performancereservation.domain.user.entitiy.User
import me.performancereservation.domain.user.repository.UserRepository
import me.performancereservation.global.exception.ErrorCode
import net.nurigo.sdk.NurigoApp
import net.nurigo.sdk.message.model.Message
import net.nurigo.sdk.message.request.SingleMessageSendingRequest
import net.nurigo.sdk.message.service.DefaultMessageService
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger {}

@Service
class SMSService(
    private val userRepository: UserRepository,
    private val performanceRepository: PerformanceRepository,
    @Value("\${coolsms.api.key}") apiKey: String,
    @Value("\${coolsms.api.secret}") apiSecret: String,
    private val reservationRepository: ReservationRepository
) {
    private val messageService: DefaultMessageService

    @Value("\${coolsms.api.number}")
    private val fromNumber: String? = null

    init {
        this.messageService = NurigoApp.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr")
    }

    //공연 승인 안내 문자
    fun performanceConfirmed(performance: Performance, user: User) {
        sendSMS(
            parsePhoneNumber(user.phoneNumber),
            "간편한 티켓 예매는 TICKET 4 U\n" +
                    "신청하신 공연 (" + performance.title + ") 가 승인 되었습니다."
        )
    }

    // 공연 거부 안내 문자
    fun performanceRejected(performance: Performance, user: User) {
        sendSMS(
            parsePhoneNumber(user.phoneNumber),
            "간편한 티켓 예매는 TICKET 4 U\n" +
                    "신청하신 공연 (" + performance.title + ") 가 거부 되었습니다."
        )
    }

    // 공연 관리자 승인 안내 문자
    fun managerRequestApproved(user: User) {
        sendSMS(
            parsePhoneNumber(user.phoneNumber),
            "간편한 티켓 예매는 TICKET 4 U\n" +
                    "공연 관리자 신청이 승인되었습니다."
        )
    }

    // 공연 관리자 거부 안내 문자
    fun managerRequestRejected(user: User) {
        sendSMS(
            parsePhoneNumber(user.phoneNumber),
            "간편한 티켓 예매는 TICKET 4 U\n" +
                    "공연 관리자 신청이 거부되었습니다."
        )
    }

    // 예약 승인 안내 문자
    fun reservationConfirmed(reservation: Reservation) {
        val performance = performanceRepository.findByIdOrNull(reservation.performanceId)
            ?: throw ErrorCode.PERFORMANCE_NOT_FOUND.domainException("해당하는 공연을 찾을 수 없습니다. id=" + reservation.performanceId)

        val user = userRepository.findByIdOrNull(reservation.userId)
            ?: throw ErrorCode.USER_NOT_FOUND.domainException("해당하는 사용자를 찾을 수 없습니다. id=" + reservation.userId)

        sendSMS(
            parsePhoneNumber(user.phoneNumber),
            "간편한 티켓 예매는 TICKET 4 U\n" +
                    "공연 " + performance.title + "에 대한 예매가 승인 되었습니다.\n" +
                    "예매 번호: " + reservation.id + "\n" +
                    "티켓 수량: " + reservation.quantity
        )
    }

    //환불 승인 안내 문자
    fun refundConfirmed(refund: Refund) {

        val user = userRepository.findByIdOrNull(refund.userId)
            ?: throw ErrorCode.USER_NOT_FOUND.domainException("해당하는 사용자를 찾을 수 없습니다. id=" + refund.userId)

        val reservation = reservationRepository.findByIdOrNull(refund.reservationId)
            ?: throw ErrorCode.RESERVATION_NOT_FOUND.domainException("예약이 존재하지 않습니다.")

        val performance = performanceRepository.findByIdOrNull(reservation.performanceId)
            ?: throw ErrorCode.PERFORMANCE_NOT_FOUND.domainException("해당하는 공연을 찾을 수 없습니다. id=" + reservation.performanceId)

        sendSMS(
            parsePhoneNumber(user.phoneNumber),
            "간편한 티켓 예매는 TICKET 4 U\n" +
                    "공연 환불이 승인 되었습니다.\n" +
                    "예매 번호: " + reservation.id + "\n" +
                    "공연 제목: " + performance.title + "\n" +
                    "총 환불 금액: " + reservation.quantity * performance.price
        )
    }

    // 정산 완료 안내 문자
    fun settlementsConfirmed(settlement: Settlement, performance: Performance) {

        val user = userRepository.findByIdOrNull(performance.managerId)
            ?: throw ErrorCode.USER_NOT_FOUND.domainException("해당하는 사용자를 찾을 수 없습니다. id=" + performance.managerId)

        sendSMS(
            parsePhoneNumber(user.phoneNumber),
            "간편한 티켓 예매는 TICKET 4 U\n" +
                    "공연 환불이 승인 되었습니다.\n" +
                    "공연 제목: " + performance.title + "\n" +
                    "공연 시작 일시: " + performance.startDate +
                    "총 정산 금액: " + settlement.totalAmount
        )
    }

    // 공연 취소 사용자 안내 문자
    fun performanceCanceled(reservation: Reservation) {

        val user = userRepository.findByIdOrNull(reservation.userId)
            ?: throw ErrorCode.USER_NOT_FOUND.domainException("해당하는 사용자를 찾을 수 없습니다. id=" + reservation.userId)

        val performance = performanceRepository.findByIdOrNull(reservation.performanceId)
            ?: throw ErrorCode.PERFORMANCE_NOT_FOUND.domainException("해당하는 공연을 찾을 수 없습니다. id=" + reservation.performanceId)

        sendSMS(
            parsePhoneNumber(user.phoneNumber),
            "간편한 티켓 예매는 TICKET 4 U\n" +
                    "다음 예매가 취소 되었습니다.\n" +
                    "마이 페이지에서 환불 받을 계좌 정보를 입력해주세요.\n" +
                    "예매 번호: " + reservation.id + "\n" +
                    "공연 제목: " + performance.title + "\n" +
                    "환불 예정 금액: " + reservation.quantity * performance.price
        )
    }


    // 요청받은 메시지 전송하기
    private fun sendSMS(phoneNumber: String?, message: String?) {
        // 전화번호가 null이거나 빈 문자열인 경우 전송하지 않음
        if (phoneNumber.isNullOrBlank()) {
            log.error("전화번호가 없어 SMS 전송을 건너뜁니다. message: {}", message)
            return
        }

        // 메시지를 만들고 발신, 수신번호 설정
        val coolsms = Message()
        coolsms.from = fromNumber
        coolsms.to = phoneNumber

        // 매개변수로 받은 문자열로 메시지 셋팅
        coolsms.text = message

        // 문자 하나 전송
        val response = this.messageService.sendOne(SingleMessageSendingRequest(coolsms))
        log.info { "${"response = {}"} $response"}
    }

    // 사용자 전화 번호의 '-' 제거
    private fun parsePhoneNumber(phoneNumber: String?): String? {
        if (phoneNumber.isNullOrBlank()) {
            return null
        }
        return phoneNumber.replace("-", "")
    }
}