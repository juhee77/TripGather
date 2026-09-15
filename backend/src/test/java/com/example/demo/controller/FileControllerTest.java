package com.example.demo.controller;

import com.example.demo.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileController fileController;

    @BeforeEach
    void setUp() {
        // standaloneSetup 은 Spring Boot 자동 구성을 타지 않아 StringHttpMessageConverter 가
        // ISO-8859-1 로 동작한다. 운영과 동일하게 한글 본문을 검증하려면 UTF-8 로 맞춰야 한다.
        mockMvc = MockMvcBuilders.standaloneSetup(fileController)
                .setMessageConverters(
                        new StringHttpMessageConverter(StandardCharsets.UTF_8),
                        new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    @DisplayName("파일 업로드 API 성공 시 저장된 URL 반환")
    void uploadFile_Success() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "데이터".getBytes());
        given(fileService.storeFile(any())).willReturn("/uploads/uuid.jpg");

        // when & then
        mockMvc.perform(multipart("/api/files/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("/uploads/uuid.jpg"));
    }

    @Test
    @DisplayName("빈 파일 업로드 시 400 반환")
    void uploadFile_EmptyFile_Returns400() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        // when & then
        mockMvc.perform(multipart("/api/files/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("파일이 비어있습니다."));
    }

    @Test
    @DisplayName("파일 저장 중 예외 발생 시 500 반환")
    void uploadFile_StorageFails_Returns500() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "데이터".getBytes());
        given(fileService.storeFile(any())).willThrow(new RuntimeException("스토리지 연결 실패"));

        // when & then
        mockMvc.perform(multipart("/api/files/upload").file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("파일 업로드 실패")));
    }
}
