package com.example.demo.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * 로컬 업로드 파일의 저장 위치와 공개 경로를 한 자리에서 결정한다.
 *
 * <p>이전에는 저장 위치가 {@code LocalStorageStrategy} 안의 상수({@code ".storage"})였고
 * 정적 서빙 경로는 {@code WebConfig} 가 같은 문자열을 따로 적어 두고 있었다.
 * 한쪽만 고치면 업로드는 성공하고 이미지는 깨지는 상태가 되므로,
 * 설정 키 {@code file.upload-dir} 를 유일한 출처로 삼는다.
 *
 * <p>빈 등록은 {@link WebConfig} 가 맡는다. 정적 서빙을 설정하는 쪽과 같은 자리에 두면
 * {@code @WebMvcTest} 슬라이스에서도 함께 올라와, 컨트롤러 테스트가 스토리지 설정을
 * 따로 챙기지 않아도 된다.
 */
public class StorageProperties {

    /** 업로드된 파일이 외부에 노출되는 URL 접두사. 저장 위치와 짝을 이룬다. */
    public static final String PUBLIC_PATH_PREFIX = "/uploads/";

    private final Path uploadDir;

    public StorageProperties(String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public Path getUploadDir() {
        return uploadDir;
    }

    /**
     * 정적 리소스 핸들러가 요구하는 디렉토리 URI.
     * {@code Path#toUri} 는 디렉토리가 아직 없으면 끝에 {@code /} 를 붙이지 않아
     * 핸들러가 경로를 이어 붙이지 못한다. 그래서 직접 보장한다.
     */
    public String getUploadDirUri() {
        String uri = uploadDir.toUri().toString();
        return uri.endsWith("/") ? uri : uri + "/";
    }

    public String toPublicUrl(String fileName) {
        return PUBLIC_PATH_PREFIX + fileName;
    }

    /**
     * 우리가 저장한 파일의 URL 이면 실제 경로를, 아니면 빈 값을 돌려준다.
     *
     * <p>외부 이미지 주소나 base64 data URI 가 그대로 들어올 수 있어 소유 여부를 먼저 가린다.
     * 또 {@code /uploads/../../etc/passwd} 처럼 업로드 디렉토리를 벗어나려는 입력도
     * 여기서 끊는다 — 삭제 경로가 열리면 그대로 임의 파일 삭제가 된다.
     */
    public Optional<Path> resolveManagedFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(PUBLIC_PATH_PREFIX)) {
            return Optional.empty();
        }
        String fileName = fileUrl.substring(PUBLIC_PATH_PREFIX.length());
        if (fileName.isBlank() || fileName.contains("/") || fileName.contains("\\") || fileName.contains("..")) {
            return Optional.empty();
        }
        Path resolved = uploadDir.resolve(fileName).normalize();
        if (!resolved.startsWith(uploadDir)) {
            return Optional.empty();
        }
        return Optional.of(resolved);
    }
}
