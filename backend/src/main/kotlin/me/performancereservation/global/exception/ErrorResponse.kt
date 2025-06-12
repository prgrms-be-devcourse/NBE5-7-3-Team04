package me.performancereservation.global.exception

data class ErrorResponse(val message: String)  {
    companion object {
        fun from(errorCode: ErrorCode): ErrorResponse = ErrorResponse(errorCode.message)
    }
}