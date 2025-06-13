package me.performancereservation.domain.file.dto

@JvmRecord
data class UploadFileResponse(
    val id: Long,
    val key: String
)
