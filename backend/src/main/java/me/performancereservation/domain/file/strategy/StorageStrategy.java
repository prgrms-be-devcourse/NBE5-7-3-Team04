package me.performancereservation.domain.file.strategy;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageStrategy {
    void upload(MultipartFile file, String newFileName) throws IOException;
}