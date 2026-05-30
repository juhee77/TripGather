package com.example.demo.service;

import com.example.demo.service.storage.StorageStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private StorageStrategy storageStrategy;

    @InjectMocks
    private FileService fileService;

    @Test
    @DisplayName("파일 저장 성공 테스트 - 스토리지 전략 위임 검증")
    void storeFile_Success() throws Exception {
        // given
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test data".getBytes());
        String expectedUrl = "http://localhost:9000/tripgather/random-uuid.jpg";
        given(storageStrategy.storeFile(eq(file), any(String.class))).willReturn(expectedUrl);

        // when
        String url = fileService.storeFile(file);

        // then
        assertThat(url).isEqualTo(expectedUrl);
        verify(storageStrategy, times(1)).storeFile(eq(file), any(String.class));
    }

    @Test
    @DisplayName("파일 삭제 성공 테스트 - 스토리지 전략 위임 검증")
    void deleteFile_Success() throws Exception {
        // given
        String fileUrl = "http://localhost:9000/tripgather/test-file.jpg";

        // when & then
        assertDoesNotThrow(() -> fileService.deleteFile(fileUrl));
        verify(storageStrategy, times(1)).deleteFile(fileUrl);
    }
}
