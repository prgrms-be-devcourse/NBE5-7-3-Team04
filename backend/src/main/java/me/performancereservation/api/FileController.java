package me.performancereservation.api;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.file.FileService;
import me.performancereservation.domain.file.dto.UploadFileResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping
    public ResponseEntity<UploadFileResponse> uploadFile(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        UploadFileResponse result = fileService.upload(file);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
