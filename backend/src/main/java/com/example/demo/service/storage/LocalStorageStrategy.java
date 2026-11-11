package com.example.demo.service.storage;

import com.example.demo.config.StorageProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Slf4j
@Component
@ConditionalOnProperty(name = "storage.type", havingValue = "local")
@RequiredArgsConstructor
public class LocalStorageStrategy implements StorageStrategy {

    private final StorageProperties storageProperties;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(storageProperties.getUploadDir());
            log.info("로컬 파일 스토리지 디렉토리 준비 완료: {}", storageProperties.getUploadDir());
        } catch (IOException e) {
            throw new RuntimeException("로컬 스토리지 초기화 실패", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String uniqueFileName) {
        try {
            Path targetLocation = storageProperties.getUploadDir().resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation);
            return storageProperties.toPublicUrl(uniqueFileName);
        } catch (IOException e) {
            throw new RuntimeException("로컬 파일 업로드 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isManaged(String fileUrl) {
        return storageProperties.resolveManagedFile(fileUrl).isPresent();
    }

    @Override
    public void deleteFile(String fileUrl) {
        Optional<Path> target = storageProperties.resolveManagedFile(fileUrl);
        if (target.isEmpty()) {
            // 우리가 저장한 파일이 아니다. 조용히 넘긴다 — 업로드 디렉토리 밖을 건드리지 않는다.
            log.debug("로컬 스토리지가 관리하지 않는 주소라 삭제를 건너뛴다: {}", fileUrl);
            return;
        }
        try {
            Files.deleteIfExists(target.get());
            log.info("로컬 파일 삭제: {}", target.get().getFileName());
        } catch (IOException e) {
            throw new RuntimeException("로컬 파일 삭제 실패: " + e.getMessage(), e);
        }
    }
}
