package com.example.demo.service;

import com.example.demo.service.storage.StorageStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
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

    @Test
    @DisplayName("파일 저장 시 확장자가 없는 원본 파일명은 UUID만으로 고유 파일명을 생성")
    void storeFile_NoExtension_UsesUuidOnly() {
        // given
        MultipartFile file = new MockMultipartFile("file", "noextension", "application/octet-stream", "raw".getBytes());
        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        given(storageStrategy.storeFile(eq(file), any(String.class))).willReturn("/uploads/generated");

        // when
        fileService.storeFile(file);

        // then
        verify(storageStrategy).storeFile(eq(file), nameCaptor.capture());
        assertThat(nameCaptor.getValue()).doesNotContain(".");
    }

    @Test
    @DisplayName("파일 저장 시 숨김 파일(.gitignore)은 확장자로 취급하지 않음")
    void storeFile_DotFile_NotTreatedAsExtension() {
        // given
        MultipartFile file = new MockMultipartFile("file", ".gitignore", "text/plain", "raw".getBytes());
        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        given(storageStrategy.storeFile(eq(file), any(String.class))).willReturn("/uploads/generated");

        // when
        fileService.storeFile(file);

        // then
        verify(storageStrategy).storeFile(eq(file), nameCaptor.capture());
        assertThat(nameCaptor.getValue()).doesNotContain(".gitignore");
    }

    @Test
    @DisplayName("파일 저장 시 스토리지 전략에서 예외 발생하면 업로드 실패 예외로 변환")
    void storeFile_StorageFails_ThrowsRuntimeException() {
        // given
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test data".getBytes());
        given(storageStrategy.storeFile(eq(file), any(String.class)))
                .willThrow(new RuntimeException("스토리지 연결 실패"));

        // when & then
        assertThatThrownBy(() -> fileService.storeFile(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("파일 업로드 실패");
    }

    @Test
    @DisplayName("파일 삭제 시 URL이 null이면 스토리지 호출 없이 무시")
    void deleteFile_NullUrl_DoesNothing() {
        // when & then
        assertDoesNotThrow(() -> fileService.deleteFile(null));
        verify(storageStrategy, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("파일 삭제 시 URL이 빈 문자열이면 스토리지 호출 없이 무시")
    void deleteFile_EmptyUrl_DoesNothing() {
        // when & then
        assertDoesNotThrow(() -> fileService.deleteFile(""));
        verify(storageStrategy, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("파일 삭제 시 스토리지 전략에서 예외 발생하면 삭제 실패 예외로 변환")
    void deleteFile_StorageFails_ThrowsRuntimeException() {
        // given
        String fileUrl = "http://localhost:9000/tripgather/test-file.jpg";
        willThrow(new RuntimeException("스토리지 연결 실패")).given(storageStrategy).deleteFile(fileUrl);

        // when & then
        assertThatThrownBy(() -> fileService.deleteFile(fileUrl))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("파일 삭제 실패");
    }
}
