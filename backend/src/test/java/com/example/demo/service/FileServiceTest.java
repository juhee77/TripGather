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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.BDDMockito.willThrow;

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

    @Test
    @DisplayName("확장자가 없는 파일명도 UUID 파일명으로 저장된다")
    void storeFile_WithoutExtension() {
        // given
        MultipartFile file = new MockMultipartFile("file", "README", "text/plain", "x".getBytes());
        given(storageStrategy.storeFile(any(MultipartFile.class), any(String.class))).willReturn("/uploads/uuid");

        // when
        String result = fileService.storeFile(file);

        // then
        assertThat(result).isEqualTo("/uploads/uuid");
        verify(storageStrategy, times(1)).storeFile(any(MultipartFile.class), any(String.class));
    }

    @Test
    @DisplayName("원본 파일명이 없으면 업로드 예외로 변환한다")
    void storeFile_NullOriginalFilename_ThrowsException() {
        // given
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        given(file.getOriginalFilename()).willReturn(null);

        // when & then
        assertThatThrownBy(() -> fileService.storeFile(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("파일 업로드 실패");
    }

    @Test
    @DisplayName("스토리지 전략이 실패하면 업로드 예외로 변환한다")
    void storeFile_StorageFails_ThrowsException() {
        // given
        MultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", "x".getBytes());
        given(storageStrategy.storeFile(any(MultipartFile.class), any(String.class)))
                .willThrow(new RuntimeException("bucket down"));

        // when & then
        assertThatThrownBy(() -> fileService.storeFile(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("파일 업로드 실패");
    }

    @Test
    @DisplayName("빈 URL 삭제 요청은 스토리지를 호출하지 않는다")
    void deleteFile_BlankUrl_SkipsStorage() {
        // when
        fileService.deleteFile(null);
        fileService.deleteFile("");

        // then
        verify(storageStrategy, never()).deleteFile(any(String.class));
    }

    @Test
    @DisplayName("스토리지 삭제가 실패하면 삭제 예외로 변환한다")
    void deleteFile_StorageFails_ThrowsException() {
        // given
        willThrow(new RuntimeException("not reachable")).given(storageStrategy).deleteFile(eq("/uploads/a.png"));

        // when & then
        assertThatThrownBy(() -> fileService.deleteFile("/uploads/a.png"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("파일 삭제 실패");
    }
}
