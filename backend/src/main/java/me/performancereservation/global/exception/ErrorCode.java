package me.performancereservation.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 시스템
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // 인증 관련

    // 파일 관련
    INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "파일 업로드 형식이 잘못되었습니다."),
    MISSING_FILE_PARAMETER(HttpStatus.BAD_REQUEST, "요청에 'file' 파라미터가 포함되어야 합니다."),
    UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST, "이미지 파일만 업로드 가능합니다."),
    FILE_EXTENSION_MISSING(HttpStatus.BAD_REQUEST, "확장자가 없는 파일로 요청했습니다."),
    EMPTY_FILE_UPLOAD(HttpStatus.BAD_REQUEST, "빈 파일은 업로드할 수 없습니다."),

    // 유저 관련

    // 공연 관련

    // 예약 관련
    INVALID_RESERVATION_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 예약 상태입니다."),

    // 환불 관련
    REFUND_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 환불ID입니다."),
    DUPLICATE_REFUND(HttpStatus.CONFLICT, "이미 존재하는 환불내역입니다."),
    INVALID_REFUND_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 환불 상태입니다.");

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