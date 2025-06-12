package me.performancereservation.global.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MultipartException
import org.springframework.web.multipart.support.MissingServletRequestPartException

private val log = KotlinLogging.logger {}

@RestControllerAdvice
class GlobalExceptionHandler {

    // 커스텀 예외
    @ExceptionHandler(AppException::class)
    fun handleBusinessException(ex: AppException): ResponseEntity<ErrorResponse> {
        val errorCode = ex.errorCode

        log.error { "[${ex.errorType}] ${ex.errorCode.name} - ${ex.developerMessage}" }

        return ResponseEntity
            .status(errorCode.httpStatus)
            .body(ErrorResponse.from(errorCode))
    }

    // 파일 관련
    @ExceptionHandler(MultipartException::class)
    fun handleMultipartException(ex: MultipartException?): ResponseEntity<ErrorResponse> {
        val errorCode = ErrorCode.INVALID_FILE_FORMAT

        return ResponseEntity
            .status(400)
            .body(ErrorResponse.from(errorCode))
    }

    @ExceptionHandler(MissingServletRequestPartException::class)
    fun handleMissingParam(ex: MissingServletRequestPartException?): ResponseEntity<ErrorResponse> {
        val errorCode = ErrorCode.MISSING_FILE_PARAMETER

        return ResponseEntity
            .status(400)
            .body(ErrorResponse.from(errorCode))
    }


    // Validation
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errorMessage = ex.bindingResult
            .allErrors
            .firstOrNull()
            ?.defaultMessage ?: "유효성 검사 실패"

        return ResponseEntity
            .status(400)
            .body(ErrorResponse(errorMessage))
    }

    // 예상못한 예외
    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(ex: Exception?): ResponseEntity<ErrorResponse> {
        val errorCode = ErrorCode.INTERNAL_SERVER_ERROR

        log.error(ex) { "Unhandled exception" }

        return ResponseEntity
            .status(errorCode.httpStatus)
            .body(ErrorResponse.from(errorCode))
    }
}
