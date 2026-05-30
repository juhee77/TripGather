package com.example.demo.service.storage;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@ConditionalOnProperty(name = "storage.type", havingValue = "local")
public class LocalStorageStrategy implements StorageStrategy {

    private static final String STORAGE_DIR = ".storage";
    private final Path storageLocation = Paths.get(STORAGE_DIR).toAbsolutePath().normalize();

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.storageLocation);
            System.out.println("로컬 파일 스토리지 디렉토리 생성 완료: " + this.storageLocation);
        } catch (IOException e) {
            throw new RuntimeException("로컬 스토리지 초기화 실패", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String uniqueFileName) {
        try {
            Path targetLocation = this.storageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation);
            // 로컬 서버 정적 서빙 및 API 호환용 임시 경로 반환
            return "/uploads/" + uniqueFileName;
        } catch (IOException e) {
            throw new RuntimeException("로컬 파일 업로드 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
            Path filePath = this.storageLocation.resolve(fileName);
            Files.deleteIfExists(filePath);
            System.out.println("로컬 파일 삭제 성공: " + fileName);
        } catch (IOException e) {
            throw new RuntimeException("로컬 파일 삭제 실패: " + e.getMessage(), e);
        }
    }
}
