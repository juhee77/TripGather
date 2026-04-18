package com.example.demo.service;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private FileService fileService;

    private final String endpoint = "http://localhost:9000";
    private final String bucket = "tripgather";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileService, "endpoint", endpoint);
        ReflectionTestUtils.setField(fileService, "bucket", bucket);
    }

    @Test
    @DisplayName("파일 저장 성공 테스트")
    void storeFile_Success() throws Exception {
        // given
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image data".getBytes());
        given(minioClient.putObject(any(PutObjectArgs.class))).willReturn(null);

        // when
        String url = fileService.storeFile(file);

        // then
        assertThat(url).startsWith(endpoint + "/" + bucket + "/");
        assertThat(url).endsWith(".jpg");
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("파일 삭제 성공 테스트")
    void deleteFile_Success() throws Exception {
        // given
        String fileUrl = endpoint + "/" + bucket + "/test-file.jpg";

        // when & then
        assertDoesNotThrow(() -> fileService.deleteFile(fileUrl));
        verify(minioClient, times(1)).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    @DisplayName("초기화 로직 버킷 존재 테스트")
    void init_BucketExists() throws Exception {
        // given
        given(minioClient.bucketExists(any(BucketExistsArgs.class))).willReturn(true);

        // when & then
        assertDoesNotThrow(() -> fileService.init());
        verify(minioClient, times(1)).bucketExists(any(BucketExistsArgs.class));
    }
}
