package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 전역 CORS 설정 및 로컬 업로드 파일 정적 서빙 설정.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @org.springframework.beans.factory.annotation.Value("${app.frontend-url}")
    private String frontendUrl;

    @org.springframework.beans.factory.annotation.Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 업로드 경로 설정을 빈으로 올린다. 정적 서빙을 설정하는 이 클래스가 같은 값을 쓰므로,
     * 두 곳이 어긋날 수 없도록 출처를 여기 하나로 둔다.
     */
    @Bean
    public StorageProperties storageProperties() {
        return new StorageProperties(uploadDir);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(frontendUrl)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 저장 위치와 공개 경로는 StorageProperties 한 곳에서 가져온다.
        registry.addResourceHandler(StorageProperties.PUBLIC_PATH_PREFIX + "**")
                .addResourceLocations(storageProperties().getUploadDirUri());
    }
}
