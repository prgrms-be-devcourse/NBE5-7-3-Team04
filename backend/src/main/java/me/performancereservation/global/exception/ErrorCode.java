package me.performancereservation.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 시스템
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // 인증 관련

    // 유저 관련
    USER_EMAIL_DUPLICATED(HttpStatus.CONFLICT,"이미 존재하는 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,"존재하지 않는 유저입니다."),
    AUTH_SOCIAL_DUPLICATED(HttpStatus.CONFLICT,"이미 연결된 소셜 로그인 플랫폼입니다.");
    // 공연 관련

    // 예약 관련

    // 환불 관련

    // 정산 관련


    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public AppException serviceException() {
        return new AppException(this, ErrorType.SERVICE);
    }

    public AppException serviceException(String detail) {
        return new AppException(this, detail, ErrorType.SERVICE);
    }

    public AppException domainException(String detail) {
        return new AppException(this, detail, ErrorType.DOMAIN);
    }

    public AppException persistenceException(String detail) {
        return new AppException(this, detail, ErrorType.PERSISTENCE);
    }

}