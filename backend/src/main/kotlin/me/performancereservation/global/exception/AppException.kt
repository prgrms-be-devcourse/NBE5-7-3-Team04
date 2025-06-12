package me.performancereservation.global.exception

class AppException(
    val errorCode: ErrorCode,
    val developerMessage: String? = null,
    val errorType: ErrorType
) : RuntimeException(errorCode.message)