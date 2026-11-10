package com.example.demo.service;

import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.service.storage.StorageStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final StorageStrategy storageStrategy;

    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "업로드할 파일이 올바르지 않거나 비어 있습니다.");
        }
        if (file.getOriginalFilename() == null || file.getOriginalFilename().trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "업로드할 파일명이 올바르지 않습니다.");
        }
        try {
            // 공통 비즈니스: 파일명 정제 및 UUID 고유 파일명 생성
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = "";
            int i = originalFileName.lastIndexOf('.');
            if (i > 0) {
                extension = originalFileName.substring(i);
            }
            String uniqueFileName = UUID.randomUUID().toString() + extension;

            // 저장소 전략 실행 (MinIO, S3 or Local)
            return storageStrategy.storeFile(file, uniqueFileName);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 업로드 실패: " + e.getMessage());
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.trim().isEmpty()) {
                return;
            }
            // 저장소 전략 삭제 위임
            storageStrategy.deleteFile(fileUrl);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 삭제 실패: " + e.getMessage());
        }
    }

    /**
     * 게시글·리뷰가 지워질 때 딸린 원본 파일도 같이 지운다.
     *
     * <p>두 가지를 지킨다.
     * <ul>
     *   <li>커밋 이후에 지운다 — 트랜잭션이 되돌려지면 레코드는 살아 있는데 파일만 없는 상태가 된다.</li>
     *   <li>실패해도 본 작업을 막지 않는다 — 저장소 장애로 게시글 삭제가 거부되면 사용자가 손해다.
     *       대신 주소를 로그에 남겨 나중에 정리할 수 있게 한다.</li>
     * </ul>
     */
    public void deleteAfterCommit(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank() || !storageStrategy.isManaged(fileUrl)) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteQuietly(fileUrl);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteQuietly(fileUrl);
            }
        });
    }

    /** 쉼표로 이어진 주소 목록(리뷰 사진 등)을 한 번에 넘긴다. */
    public void deleteAllAfterCommit(String commaSeparatedUrls) {
        if (commaSeparatedUrls == null || commaSeparatedUrls.isBlank()) {
            return;
        }
        for (String url : commaSeparatedUrls.split(",")) {
            deleteAfterCommit(url.trim());
        }
    }

    private void deleteQuietly(String fileUrl) {
        try {
            storageStrategy.deleteFile(fileUrl);
        } catch (Exception e) {
            log.warn("원본 파일 삭제 실패. 레코드는 이미 지워졌으니 저장소에 남는다. url={}", fileUrl, e);
        }
    }
}
