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

    // 공연 승인 관련
    PERFORMANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 공연을 찾을 수 없습니다."),
    PERFORMANCE_STATUS_NOT_PENDING(HttpStatus.BAD_REQUEST, "PENDING 상태의 공연만 승인, 거부 할 수 있습니다."),

    //공연자 승인 관련
    MANAGER_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 공연 관리자 요청을 찾을 수 없습니다."),
    MANAGER_REQUEST_STATUS_NOT_PENDING(HttpStatus.BAD_REQUEST, "PENDING 상태의 공연 관리자 요청만 승인, 거부 할 수 있습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 사용자를 찾을 수 없습니다.");

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