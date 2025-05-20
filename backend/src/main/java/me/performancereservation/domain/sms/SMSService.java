package me.performancereservation.domain.sms;

import lombok.extern.slf4j.Slf4j;
import me.performancereservation.domain.performance.entities.Performance;
import me.performancereservation.domain.performance.repository.PerformanceRepository;
import me.performancereservation.domain.performance.service.PerformanceService;
import me.performancereservation.domain.refund.Refund;
import me.performancereservation.domain.reservation.Reservation;
import me.performancereservation.domain.reservation.ReservationRepository;
import me.performancereservation.domain.settlement.Settlement;
import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.domain.user.repository.UserRepository;
import me.performancereservation.global.exception.ErrorCode;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.Random;

@Slf4j
@Service
public class SMSService {

    private final UserRepository userRepository;
    private final DefaultMessageService messageService;
    private final ReservationRepository reservationRepository;
    private final PerformanceRepository performanceRepository;

    public SMSService(UserRepository userRepository,
                      PerformanceRepository performanceRepository,
                      @Value("${coolsms.api.key}") String apiKey,
                      @Value("${coolsms.api.secret}") String apiSecret,
                      ReservationRepository reservationRepository) {
        this.userRepository = userRepository;
        this.performanceRepository = performanceRepository;
        this.reservationRepository = reservationRepository;
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
    }

    @Value("${coolsms.api.number}")
    private String fromNumber;

    //공연 승인 안내 문자
    public void performanceConfirmed(Performance performance, User user) {
        sendSMS(user.getPhoneNumber(),
                "간편한 티켓 예매는 TICKET 4 U\n" +
                "신청하신 공연 (" + performance.getTitle() + ") 가 승인 되었습니다.");
    }

    // 공연 거부 안내 문자
    public void performanceRejected(Performance performance, User user) {
        sendSMS(user.getPhoneNumber(),
                "간편한 티켓 예매는 TICKET 4 U\n" +
                "신청하신 공연 (" + performance.getTitle() + ") 가 거부 되었습니다.");
    }

    // 공연 관리자 승인 안내 문자
    public void managerRequestApproved(User user) {
        sendSMS(user.getPhoneNumber(),
                "간편한 티켓 예매는 TICKET 4 U\n" +
                        "공연 관리자 신청이 승인되었습니다.");
    }

    // 공연 관리자 거부 안내 문자
    public void managerRequestRejected(User user) {
        sendSMS(user.getPhoneNumber(),
                "간편한 티켓 예매는 TICKET 4 U\n" +
                        "공연 관리자 신청이 거부되었습니다.");
    }

    // 예약 승인 안내 문자
    public void reservationConfirmed(Reservation reservation) {
        Performance performance = performanceRepository.findById(reservation.getPerformanceId())
                .orElseThrow(() -> ErrorCode.PERFORMANCE_NOT_FOUND.domainException("해당하는 공연을 찾을 수 없습니다. id=" + reservation.getPerformanceId()));

        User user = userRepository.findById(reservation.getUserId())
                .orElseThrow(() -> ErrorCode.USER_NOT_FOUND.domainException("해당하는 사용자를 찾을 수 없습니다."));

        sendSMS(user.getPhoneNumber(),
                "간편한 티켓 예매는 TICKET 4 U\n" +
                        "공연 " + performance.getTitle() + "에 대한 예매가 승인 되었습니다.\n" +
                        "티켓 번호:" + reservation.getId());
    }

    //환불 승인 안내 문자
    public void refundConfirmed(Refund refund) {
        User user = userRepository.findById(refund.getUserId())
                .orElseThrow(() -> ErrorCode.USER_NOT_FOUND.domainException("해당하는 사용자를 찾을 수 없습니다."));
        Reservation reservation = reservationRepository.findById(refund.getReservationId())
                .orElseThrow(() -> ErrorCode.RESERVATION_NOT_FOUND.domainException("예약이 존재하지 않습니다."));;
        Performance performance = performanceRepository.findById(reservation.getPerformanceId())
                .orElseThrow(() -> ErrorCode.PERFORMANCE_NOT_FOUND.domainException("해당하는 공연을 찾을 수 없습니다. id=" + reservation.getPerformanceId()));

        sendSMS(user.getPhoneNumber(),
                "간편한 티켓 예매는 TICKET 4 U\n" +
                        "공연 환불이 승인 되었습니다.\n" +
                        "예매 번호: " + reservation.getId() + "\n" +
                        "공연 제목: " + performance.getTitle() + "\n" +
                        "총 환불 금액: " + reservation.getQuantity() * performance.getPrice());
    }

    // 정산 완료 안내 문자
    public void settlementsConfirmed(Settlement settlement, Performance performance) {
        User user = userRepository.findById(performance.getManagerId())
                .orElseThrow(() -> ErrorCode.USER_NOT_FOUND.domainException("해당하는 사용자를 찾을 수 없습니다."));

        sendSMS(user.getPhoneNumber(),
                "간편한 티켓 예매는 TICKET 4 U\n" +
                        "공연 환불이 승인 되었습니다.\n" +
                        "공연 제목: " + performance.getTitle() + "\n" +
                        "공연 시작 일시: " + performance.getStartDate() +
                        "총 정산 금액: " + settlement.getTotalAmount());
    }

    // 공연 취소 사용자 안내 문자
    public void performanceCanceled(Reservation reservation) {
        User user = userRepository.findById(reservation.getUserId())
                .orElseThrow(() -> ErrorCode.USER_NOT_FOUND.domainException("해당하는 사용자를 찾을 수 없습니다."));
        Performance performance = performanceRepository.findById(reservation.getPerformanceId())
                .orElseThrow(() -> ErrorCode.PERFORMANCE_NOT_FOUND.domainException("해당하는 공연을 찾을 수 없습니다. id=" + reservation.getPerformanceId()));

        sendSMS(user.getPhoneNumber(),
                "간편한 티켓 예매는 TICKET 4 U\n" +
                        "다음 예매가 취소 되었습니다.\n" +
                        "마이 페이지에서 환불 받을 계좌 정보를 입력해주세요.\n" +
                        "예매 번호: " + reservation.getId() + "\n" +
                        "공연 제목: " + performance.getTitle() + "\n" +
                        "환불 예정 금액: " + reservation.getQuantity() * performance.getPrice());
    }


    // 요청받은 메시지 전송하기
    private void sendSMS(String phoneNumber, String message) {
        // 메시지를 만들고 발신, 수신번호 설정
        Message coolsms = new Message();
        coolsms.setFrom(fromNumber);
        coolsms.setTo(phoneNumber);

        // 메시지는 다음과 같이 셋팅해 주시면 됩니다!
        // message = "test \n티켓 번호: 123-456 \n환불규정";

        // 매개변수로 받은 문자열로 메시지 셋팅
        coolsms.setText(message);

        // 문자 하나 전송
        SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(coolsms));
        log.info("response = {}", response);
    }

}