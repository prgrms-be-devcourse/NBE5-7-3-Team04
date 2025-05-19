package me.performancereservation.domain.sms;

import lombok.extern.slf4j.Slf4j;
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

    private final DefaultMessageService messageService;

    public SMSService(@Value("${coolsms.api.key}") String apiKey, @Value("${coolsms.api.secret}") String apiSecret) {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
    }

    @Value("${coolsms.api.number}")
    private String fromNumber;

    // 요청받은 메시지 전송하기
    public void sendSMS(String phoneNumber, String message) {
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