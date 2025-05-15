package me.performancereservation.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 시스템
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // 인증 관련
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),

    // 어드민 관련
    ADMIN_AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "어드민 인증이 필요합니다."),
    UNAUTHORIZED_ADMIN(HttpStatus.FORBIDDEN, "어드민 권한이 필요합니다."),
    ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND, "어드민 아이디와 비밀번호를 확인해주세요."),

    // 파일 관련
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드를 실패했습니다."),
    INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "파일 업로드 형식이 잘못되었습니다."),
    MISSING_FILE_PARAMETER(HttpStatus.BAD_REQUEST, "요청에 'file' 파라미터가 포함되어야 합니다."),
    UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST, "이미지 파일만 업로드 가능합니다."),
    FILE_EXTENSION_MISSING(HttpStatus.BAD_REQUEST, "확장자가 없는 파일로 요청했습니다."),
    EMPTY_FILE_UPLOAD(HttpStatus.BAD_REQUEST, "빈 파일은 업로드할 수 없습니다."),

    // 유저 관련
    DUPLICATE_USER_EMAIL(HttpStatus.CONFLICT,"이미 존재하는 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,"존재하지 않는 유저입니다."),
    DUPLICATE_AUTH_SOCIAL(HttpStatus.CONFLICT,"이미 연결된 소셜 로그인 플랫폼입니다."),
    INVALID_PROVIDER(HttpStatus.BAD_REQUEST,"지원하지 않는 소셜 플랫폼 입니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED,"유효하지 않는 ACCESS 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,"유효하지 않는 REFRESH 토큰입니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AccessToken이 만료되었습니다." ),
    TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST,"존재하지 않는 토큰입니다."),
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다."),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // 공연 관련
    PERFORMANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 공연입니다."),
    PERFORMANCE_SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 공연의 회차 정보를 찾을 수 없습니다."),
    PERFORMANCE_PENDING_APPROVAL(HttpStatus.BAD_REQUEST, "공연이 승인 대기 중입니다. 승인 후 회차 등록이 가능합니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 이미지를 찾을 수 없습니다."),
    PERFORMANCE_ALREADY_CANCELED(HttpStatus.CONFLICT, "이미 취소된 공연입니다."),
    SCHEDULE_ALREADY_CANCELED(HttpStatus.CONFLICT, "이미 취소된 회차입니다."),
    INVALID_SCHEDULE_PERIOD(HttpStatus.BAD_REQUEST, "유요하지 않은 등록 기간입니다."),

    // 공연 승인 관련
    PERFORMANCE_STATUS_NOT_PENDING(HttpStatus.BAD_REQUEST, "PENDING 상태의 공연만 승인, 거부 할 수 있습니다."),

    //공연자 승인 관련
    MANAGER_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 공연 관리자 요청을 찾을 수 없습니다."),
    MANAGER_REQUEST_STATUS_NOT_PENDING(HttpStatus.BAD_REQUEST, "PENDING 상태의 공연 관리자 요청만 승인, 거부 할 수 있습니다."),

    // 예약 관련
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND,  "해당하는 예약이 없습니다."),
    ALREADY_CANCELED_RESERVATION(HttpStatus.BAD_REQUEST, "이미 취소된 예약입니다."),
    INVALID_RESERVATION_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 예약 상태입니다."),

    // 좌석 재고 관련
    RESERVATION_ALREADY_CONFIRMED(HttpStatus.CONFLICT, "이미 확정된 예약입니다."),
    NO_REMAINING_SEATS(HttpStatus.BAD_REQUEST, "남은 좌석이 없습니다."),
    SEAT_STOCK_DECREMENT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "좌석 차감에 실패했습니다."),
    SEAT_STOCK_INCREMENT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "좌석 복원에 실패했습니다."),

    // 환불 관련
    REFUND_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 환불ID입니다."),
    DUPLICATE_REFUND(HttpStatus.CONFLICT, "이미 존재하는 환불내역입니다."),
    INVALID_REFUND_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 환불 상태입니다."),
    UNAUTHORIZED_REFUND_UPDATE(HttpStatus.UNAUTHORIZED, "본인의 환불 내역만 변경할 수 있습니다."),

    // 정산 관련
    SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 정산입니다."),
    INVALID_SETTLEMENT_REQUEST(HttpStatus.BAD_REQUEST, "공연 종료 후 7일이 지나야 정산 신청이 가능합니다."),
    INVALID_SETTLEMENT_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 정산 상태입니다."),
    UNAUTHORIZED_SETTLEMENT_UPDATE(HttpStatus.UNAUTHORIZED, "본인의 정산 내역만 변경할 수 있습니다."),

    // 리뷰 관련
    DUPLICATE_REVIEW(HttpStatus.CONFLICT,"이미 리뷰를 작성하셨습니다."),
    UNAUTHORIZED_REVIEW(HttpStatus.UNAUTHORIZED,"예매한 공연에만 리뷰를 작성하실 수 있습니다."),
    INVALID_SCHEDULE(HttpStatus.BAD_REQUEST,"해당 공연에 속하지 않는 회차입니다."),

    //찜 관련
    DUPLICATED_BOOKMARK(HttpStatus.CONFLICT, "이미 같은 공연에 찜이 존재합니다."),
    BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "이미 찜이 안되어 있는 공연입니다.");

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
