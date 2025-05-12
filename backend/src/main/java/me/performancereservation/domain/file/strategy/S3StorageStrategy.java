package me.performancereservation.domain.file.strategy;

import lombok.RequiredArgsConstructor;
import me.performancereservation.global.storage.s3.S3Service;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RequiredArgsConstructor
@Component("S3Strategy")
public class S3StorageStrategy implements StorageStrategy {
    private final S3Service s3Service;

    // 도메인에 S3 라이브러리 구현체 의존하지 않으려 S3Service에 실제 구현을 두고 의존했습니다.
    @Override
    public void upload(MultipartFile file, String newFileName) throws IOException {
        s3Service.uploadFile(file, newFileName);
    }
}
