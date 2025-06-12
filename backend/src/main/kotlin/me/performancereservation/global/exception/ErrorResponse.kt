package me.performancereservation.global.exception

import lombok.AllArgsConstructor
import lombok.Getter


data class ErrorResponse(val message: String)  {
    companion object {
        fun from(errorCode: ErrorCode): ErrorResponse = ErrorResponse(errorCode.message)
    }
}