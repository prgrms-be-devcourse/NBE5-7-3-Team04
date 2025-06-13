package me.performancereservation.domain.file.strategy

import org.springframework.web.multipart.MultipartFile
import java.io.IOException

interface StorageStrategy {
    @Throws(IOException::class)
    fun upload(file: MultipartFile?, newFileName: String?)
}