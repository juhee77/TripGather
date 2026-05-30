package com.example.demo.service;

import com.example.demo.service.storage.MinioStorageStrategy;
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
class MinioStorageStrategyTest {

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private MinioStorageStrategy minioStorageStrategy;

    private final String endpoint = "http://localhost:9000";
    private final String bucket = "tripgather";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(minioStorageStrategy, "endpoint", endpoint);
        ReflectionTestUtils.setField(minioStorageStrategy, "bucket", bucket);
    }

    @Test
    @DisplayName("Minio 초기화 로직 버킷 존재 테스트")
    void init_BucketExists() throws Exception {
        // given
        given(minioClient.bucketExists(any(BucketExistsArgs.class))).willReturn(true);

        // when & then
        assertDoesNotThrow(() -> minioStorageStrategy.init());
        verify(minioClient, times(1)).bucketExists(any(BucketExistsArgs.class));
    }

    @Test
    @DisplayName("Minio 파일 저장 성공 테스트")
    void storeFile_Success() throws Exception {
        // given
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image data".getBytes());
        String uniqueFileName = "random-uuid.jpg";
        given(minioClient.putObject(any(PutObjectArgs.class))).willReturn(null);

        // when
        String url = minioStorageStrategy.storeFile(file, uniqueFileName);

        // then
        assertThat(url).isEqualTo(endpoint + "/" + bucket + "/" + uniqueFileName);
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("Minio 파일 삭제 성공 테스트")
    void deleteFile_Success() throws Exception {
        // given
        String fileUrl = endpoint + "/" + bucket + "/test-file.jpg";

        // when & then
        assertDoesNotThrow(() -> minioStorageStrategy.deleteFile(fileUrl));
        verify(minioClient, times(1)).removeObject(any(RemoveObjectArgs.class));
    }
}
