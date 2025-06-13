package me.performancereservation.domain.file

import lombok.extern.slf4j.Slf4j
import me.performancereservation.domain.file.File
import me.performancereservation.domain.file.dto.UploadFileResponse
import me.performancereservation.domain.file.strategy.StorageStrategy
import me.performancereservation.global.exception.ErrorCode
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.util.*

@Service
@Slf4j
class FileService
    (
    private val fileRepository: FileRepository,

    // @RequiredArgsConstructor는 생성자에 @Qualifier를 붙일 수 없어서 직접 생성자를 구현
    @Qualifier("S3Strategy")
    private val storageStrategy: StorageStrategy
) {
    /**
     * 파일 업로드
     *
     * @param file multipart 파일
     * @return UploadFileResponse
     */
    fun upload(file: MultipartFile): UploadFileResponse {
        if (file.isEmpty) {
            throw ErrorCode.EMPTY_FILE_UPLOAD.serviceException("빈 파일은 업로드할 수 없습니다.")
        }

        val originalFilename = file.originalFilename
        val extension = getExtension(originalFilename) // 확장자

        if (extension.isEmpty()) {
            throw ErrorCode.FILE_EXTENSION_MISSING.serviceException("확장자가 없는 파일로 요청했습니다. : $originalFilename")
        }

        val newFileName = "${UUID.randomUUID()}.$extension" // 새 파일명
        val mimeType = file.contentType

        if (!isImage(mimeType)) {
            throw ErrorCode.UNSUPPORTED_FILE_TYPE.serviceException("이미지 파일만 업로드 가능합니다. : $mimeType")
        }

        // 스토리지에 저장
        try {
            storageStrategy.upload(file, newFileName)
        } catch (e: IOException) {
            throw ErrorCode.FILE_UPLOAD_FAILED.serviceException("파일 업로드 중 문제가 발생했습니다.")
        }

        // DB에 저장
        val savedFile: File = fileRepository.save(
            File(key = newFileName)
        )

        return UploadFileResponse(savedFile.id!!, savedFile.key)
    }

    // 확장자 얻기
    private fun getExtension(filename: String?): String {
        return filename?.substringAfterLast('.', "") ?: ""
    }

    // 이미지 확장자 유효성 검사
    private fun isImage(mimeType: String?): Boolean {
        return mimeType != null && mimeType.startsWith("image")
    }
}
