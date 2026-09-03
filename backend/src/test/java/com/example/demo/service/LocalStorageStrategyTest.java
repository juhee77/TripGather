package com.example.demo.service;

import com.example.demo.service.storage.LocalStorageStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * LocalStorageStrategy 는 외부 서비스가 아닌 로컬 파일 시스템만 사용하므로
 * 모킹 대신 JUnit 의 @TempDir 을 실제 저장 위치로 주입해 검증한다.
 */
class LocalStorageStrategyTest {

    @TempDir
    Path tempDir;

    private LocalStorageStrategy localStorageStrategy;

    @BeforeEach
    void setUp() {
        localStorageStrategy = new LocalStorageStrategy();
        ReflectionTestUtils.setField(localStorageStrategy, "storageLocation", tempDir);
    }

    @Test
    @DisplayName("로컬 스토리지 초기화 시 저장 디렉토리 생성")
    void init_CreatesStorageDirectory() {
        // given
        Path storageDir = tempDir.resolve("storage");
        ReflectionTestUtils.setField(localStorageStrategy, "storageLocation", storageDir);

        // when
        localStorageStrategy.init();

        // then
        assertThat(Files.isDirectory(storageDir)).isTrue();
    }

    @Test
    @DisplayName("저장 위치에 동일 이름의 파일이 존재해 디렉토리 생성 실패 시 초기화 예외 발생")
    void init_DirectoryCreationFails_ThrowsException() throws Exception {
        // given
        Path conflicting = tempDir.resolve("conflict");
        Files.writeString(conflicting, "일반 파일");
        ReflectionTestUtils.setField(localStorageStrategy, "storageLocation", conflicting);

        // when & then
        assertThatThrownBy(() -> localStorageStrategy.init())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("로컬 스토리지 초기화 실패");
    }

    @Test
    @DisplayName("로컬 파일 저장 성공 - 정적 서빙 경로 반환")
    void storeFile_Success() throws Exception {
        // given
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "이미지 데이터".getBytes());
        String uniqueFileName = "random-uuid.jpg";

        // when
        String url = localStorageStrategy.storeFile(file, uniqueFileName);

        // then
        assertThat(url).isEqualTo("/uploads/" + uniqueFileName);
        assertThat(Files.readString(tempDir.resolve(uniqueFileName))).isEqualTo("이미지 데이터");
    }

    @Test
    @DisplayName("동일한 파일명이 이미 존재해 저장 실패 시 업로드 예외 발생")
    void storeFile_AlreadyExists_ThrowsException() throws Exception {
        // given
        String uniqueFileName = "duplicated.jpg";
        Files.writeString(tempDir.resolve(uniqueFileName), "기존 파일");
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "새 파일".getBytes());

        // when & then
        assertThatThrownBy(() -> localStorageStrategy.storeFile(file, uniqueFileName))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("로컬 파일 업로드 실패");
    }

    @Test
    @DisplayName("로컬 파일 삭제 성공 - URL 마지막 세그먼트를 파일명으로 사용")
    void deleteFile_Success() throws Exception {
        // given
        String fileName = "target.jpg";
        Path stored = tempDir.resolve(fileName);
        Files.writeString(stored, "삭제 대상");

        // when
        localStorageStrategy.deleteFile("/uploads/" + fileName);

        // then
        assertThat(Files.exists(stored)).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 파일 삭제 요청 시 예외 없이 무시")
    void deleteFile_NotExists_DoesNotThrow() {
        // when & then
        assertDoesNotThrow(() -> localStorageStrategy.deleteFile("/uploads/not-exists.jpg"));
    }

    @Test
    @DisplayName("비어있지 않은 디렉토리 삭제 시도 시 삭제 실패 예외 발생")
    void deleteFile_DeletionFails_ThrowsException() throws Exception {
        // given
        Path nonEmptyDir = tempDir.resolve("nested");
        Files.createDirectory(nonEmptyDir);
        Files.writeString(nonEmptyDir.resolve("child.txt"), "자식 파일");

        // when & then
        assertThatThrownBy(() -> localStorageStrategy.deleteFile("/uploads/nested"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("로컬 파일 삭제 실패");
    }
}
