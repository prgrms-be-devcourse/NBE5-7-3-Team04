package me.performancereservation.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        log.error("[{}] {} - {}",
                ex.getErrorType(),
                errorCode.name(),
                ex.getDeveloperMessage(),
                ex
        );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.from(errorCode));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> handleMultipartException(MultipartException ex) {
        ErrorCode errorCode = ErrorCode.INVALID_FILE_FORMAT;

        return ResponseEntity
                .status(400)
                .body(ErrorResponse.from(errorCode));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestPartException ex) {
        ErrorCode errorCode = ErrorCode.MISSING_FILE_PARAMETER;

        return ResponseEntity
                .status(400)
                .body(ErrorResponse.from(errorCode));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.from(errorCode));
    }
}
