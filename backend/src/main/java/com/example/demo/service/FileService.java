package com.example.demo.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioClient minioClient;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.bucket}")
    private String bucket;

    public String storeFile(MultipartFile file) {
        try {
            // 버킷이 존재하지 않으면 생성
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }

            // 파일명 생성
            String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            String extension = "";
            int i = originalFileName.lastIndexOf('.');
            if (i > 0) {
                extension = originalFileName.substring(i);
            }
            String fileName = UUID.randomUUID().toString() + extension;

            // 파일 업로드
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 전체 URL 반환 (예: http://localhost:9000/tripgather/filename.jpg)
            return String.format("%s/%s/%s", endpoint, bucket, fileName);

        } catch (Exception e) {
            throw new RuntimeException("MinIO 파일 업로드 실패: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            // URL에서 파일명 추출 (예: http://localhost:9000/tripgather/filename.jpg -> filename.jpg)
            String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
            
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("MinIO 파일 삭제 실패: " + e.getMessage(), e);
        }
    }
}
