package com.example.demo.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("업로드 경로 설정")
class StoragePropertiesTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("설정값을 절대 경로로 정규화한다")
    void resolvesConfiguredDirectory() {
        StorageProperties properties = new StorageProperties("uploads");

        assertThat(properties.getUploadDir())
                .isAbsolute()
                .isEqualTo(Paths.get("uploads").toAbsolutePath().normalize());
    }

    @Test
    @DisplayName("정적 리소스 위치는 디렉토리가 아직 없어도 슬래시로 끝난다")
    void uploadDirUriAlwaysEndsWithSlash() {
        StorageProperties properties = new StorageProperties(tempDir.resolve("not-created-yet").toString());

        assertThat(properties.getUploadDirUri()).endsWith("/");
    }

    @Test
    @DisplayName("저장 파일명은 공개 경로 접두사가 붙은 URL 이 된다")
    void toPublicUrl() {
        StorageProperties properties = new StorageProperties(tempDir.toString());

        assertThat(properties.toPublicUrl("photo.png")).isEqualTo("/uploads/photo.png");
    }

    @Test
    @DisplayName("공개 경로로 들어온 파일명만 실제 경로로 풀린다")
    void resolveManagedFile_OwnUrl() {
        StorageProperties properties = new StorageProperties(tempDir.toString());

        assertThat(properties.resolveManagedFile("/uploads/photo.png"))
                .contains(tempDir.resolve("photo.png"));
    }

    @Test
    @DisplayName("외부 주소·data URI·빈 파일명은 관리 대상이 아니다")
    void resolveManagedFile_ForeignUrl() {
        StorageProperties properties = new StorageProperties(tempDir.toString());

        assertThat(properties.resolveManagedFile(null)).isEmpty();
        assertThat(properties.resolveManagedFile("https://cdn.example.com/photo.png")).isEmpty();
        assertThat(properties.resolveManagedFile("data:image/png;base64,AAAA")).isEmpty();
        assertThat(properties.resolveManagedFile("/uploads/")).isEmpty();
    }

    @Test
    @DisplayName("업로드 디렉토리를 벗어나려는 경로는 거부한다")
    void resolveManagedFile_PathTraversal() {
        StorageProperties properties = new StorageProperties(tempDir.toString());

        assertThat(properties.resolveManagedFile("/uploads/../secret.txt")).isEmpty();
        assertThat(properties.resolveManagedFile("/uploads/sub/dir.png")).isEmpty();
        assertThat(properties.resolveManagedFile("/uploads/..\\secret.txt")).isEmpty();
    }
}
