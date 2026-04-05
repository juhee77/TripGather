package com.example.demo.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import jakarta.annotation.PostConstruct;
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

    @PostConstruct
    public void init() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                System.out.println("MinIO 버킷 생성 완료: " + bucket);
            }
            // 버킷 정책을 Public Read로 설정 (익명 사용자 읽기 허용)
            String policy = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetBucketLocation\",\"s3:ListBucket\"],\"Resource\":[\"arn:aws:s3:::" + bucket + "\"]},{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::" + bucket + "/*\"]}]}";
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucket).config(policy).build());
            System.out.println("MinIO 버킷 정책(Public Read) 설정 완료: " + bucket);
        } catch (Exception e) {
            System.err.println("MinIO 초기화 중 오류 발생: " + e.getMessage());
        }
    }

    public String storeFile(MultipartFile file) {
        try {
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
