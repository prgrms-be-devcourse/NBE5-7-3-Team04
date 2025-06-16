package me.performancereservation.global.storage.s3

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class S3Service(
    private val amazonS3: AmazonS3,
    @Value("\${cloud.aws.s3.bucket}")
    private val bucket: String
) {
    fun uploadFile(file: MultipartFile, key: String) {
        file.inputStream.use { inputStream ->
            val metadata = ObjectMetadata().apply {
                contentLength = file.size
                contentType = file.contentType
            }
            amazonS3.putObject(bucket, key, inputStream, metadata)
        }
    }
}