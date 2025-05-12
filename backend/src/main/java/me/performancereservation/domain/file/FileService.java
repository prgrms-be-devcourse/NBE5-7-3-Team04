package me.performancereservation.domain.file;

import lombok.extern.slf4j.Slf4j;
import me.performancereservation.domain.file.dto.UploadFileResponse;
import me.performancereservation.domain.file.strategy.StorageStrategy;
import me.performancereservation.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class FileService {
    private final FileRepository fileRepository;
    private final StorageStrategy storageStrategy;

    // @RequiredArgsConstructor는 생성자에 @Qualifier를 붙일 수 없어서 직접 생성자를 구현
    public FileService(
            FileRepository fileRepository,
            @Qualifier("S3Strategy")  StorageStrategy storageStrategy) {
        this.fileRepository = fileRepository;
        this.storageStrategy = storageStrategy;
    }

    /**
     * 파일 업로드
     *
     * @param file multipart 파일
     * @return UploadFileResponse
     */
    public UploadFileResponse upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw ErrorCode.EMPTY_FILE_UPLOAD.serviceException("빈 파일은 업로드할 수 없습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename); // 확장자

        if (extension.isEmpty()) {
            throw ErrorCode.FILE_EXTENSION_MISSING.serviceException("확장자가 없는 파일로 요청했습니다. : " + originalFilename);
        }

        String newFileName = UUID.randomUUID() + "." + extension; // 새 파일명
        String mimeType = file.getContentType();

        if (!isImage(mimeType)) {
            throw ErrorCode.UNSUPPORTED_FILE_TYPE.serviceException("이미지 파일만 업로드 가능합니다. : " + mimeType);
        }

        // 스토리지에 저장
        try {
            storageStrategy.upload(file, newFileName);
        } catch (IOException e) {
            throw ErrorCode.FILE_UPLOAD_FAILED.serviceException("파일 업로드 중 문제가 발생했습니다.");
        }

        // DB에 저장
        File savedFile = fileRepository.save(
                 File.builder()
                         .key(newFileName)
                         .build()
        );

        return new UploadFileResponse(savedFile.getId(), savedFile.getKey());
    }

    // 확장자 얻기
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ""; // 확장자 없을 때

        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    // 이미지 확장자 유효성 검사
    private boolean isImage(String mimeType) {
        return mimeType != null && mimeType.startsWith("image");
    }
}
