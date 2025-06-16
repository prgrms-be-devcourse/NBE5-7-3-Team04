package me.performancereservation.api

import me.performancereservation.api.docs.FileApiDocs
import me.performancereservation.domain.file.FileService
import me.performancereservation.domain.file.dto.UploadFileResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/files")
class FileController(
    private val fileService: FileService
) : FileApiDocs {

    @PostMapping
    override fun uploadFile(
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<UploadFileResponse> {
        val result = fileService.upload(file)

        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }
}
