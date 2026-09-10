package com.example.demo.service;

import com.example.demo.service.storage.LocalStorageStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.given;

@DisplayName("로컬 디스크 파일 스토리지 전략")
class LocalStorageStrategyTest {

    @TempDir
    Path tempDir;

    private LocalStorageStrategy localStorageStrategy;

    @BeforeEach
    void setUp() {
        localStorageStrategy = new LocalStorageStrategy();
        // 실제 프로젝트 디렉토리(.storage)를 건드리지 않도록 임시 디렉토리로 대체한다.
        ReflectionTestUtils.setField(localStorageStrategy, "storageLocation", tempDir);
    }

    @AfterEach
    void tearDown() throws IOException {
        try (var paths = Files.list(tempDir)) {
            for (Path path : paths.toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    @Test
    @DisplayName("초기화 시 스토리지 디렉토리가 준비된다")
    void init_CreatesStorageDirectory() {
        // when
        assertDoesNotThrow(() -> localStorageStrategy.init());

        // then
        assertThat(Files.isDirectory(tempDir)).isTrue();
    }

    @Test
    @DisplayName("파일 저장 성공 시 정적 서빙 경로를 반환하고 실제 파일이 기록된다")
    void storeFile_Success() throws IOException {
        // given
        MultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png", "hello".getBytes(StandardCharsets.UTF_8));

        // when
        String result = localStorageStrategy.storeFile(file, "unique-photo.png");

        // then
        assertThat(result).isEqualTo("/uploads/unique-photo.png");
        Path stored = tempDir.resolve("unique-photo.png");
        assertThat(Files.exists(stored)).isTrue();
        assertThat(Files.readString(stored)).isEqualTo("hello");
    }

    @Test
    @DisplayName("같은 이름으로 중복 저장 시 예외가 발생한다")
    void storeFile_DuplicateName_ThrowsException() {
        // given
        MultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png", "hello".getBytes(StandardCharsets.UTF_8));
        localStorageStrategy.storeFile(file, "dup.png");

        // when & then
        assertThatThrownBy(() -> localStorageStrategy.storeFile(file, "dup.png"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("로컬 파일 업로드 실패");
    }

    @Test
    @DisplayName("입력 스트림을 읽을 수 없으면 업로드 예외로 변환한다")
    void storeFile_IOException_ThrowsRuntimeException() throws IOException {
        // given
        MultipartFile file = Mockito.mock(MultipartFile.class);
        given(file.getInputStream()).willThrow(new IOException("stream broken"));

        // when & then
        assertThatThrownBy(() -> localStorageStrategy.storeFile(file, "broken.png"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("로컬 파일 업로드 실패");
    }

    @Test
    @DisplayName("URL 마지막 경로 세그먼트를 파일명으로 삼아 삭제한다")
    void deleteFile_Success() throws IOException {
        // given
        Files.writeString(tempDir.resolve("target.png"), "data");

        // when
        localStorageStrategy.deleteFile("/uploads/target.png");

        // then
        assertThat(Files.exists(tempDir.resolve("target.png"))).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 파일 삭제는 예외 없이 통과한다")
    void deleteFile_NotExists_DoesNotThrow() {
        assertDoesNotThrow(() -> localStorageStrategy.deleteFile("/uploads/missing.png"));
    }
}
