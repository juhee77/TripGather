package com.example.demo.service;

import com.example.demo.service.storage.StorageStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final StorageStrategy storageStrategy;

    public String storeFile(MultipartFile file) {
        try {
            // 공통 비즈니스: 파일명 정제 및 UUID 고유 파일명 생성
            String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            String extension = "";
            int i = originalFileName.lastIndexOf('.');
            if (i > 0) {
                extension = originalFileName.substring(i);
            }
            String uniqueFileName = UUID.randomUUID().toString() + extension;

            // 저장소 전략 실행 (MinIO, S3 or Local)
            return storageStrategy.storeFile(file, uniqueFileName);

        } catch (Exception e) {
            throw new RuntimeException("파일 업로드 실패: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                return;
            }
            // 저장소 전략 삭제 위임
            storageStrategy.deleteFile(fileUrl);
        } catch (Exception e) {
            throw new RuntimeException("파일 삭제 실패: " + e.getMessage(), e);
        }
    }
}
