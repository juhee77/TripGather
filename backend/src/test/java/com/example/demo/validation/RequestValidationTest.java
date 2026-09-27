package com.example.demo.validation;

import com.example.demo.controller.AuthController;
import com.example.demo.controller.ItineraryController;
import com.example.demo.controller.TripController;
import com.example.demo.controller.TripReviewController;
import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.ItineraryRequest;
import com.example.demo.dto.RoutePointRequest;
import com.example.demo.dto.TripRequest;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.service.TripReviewService;
import com.example.demo.service.TripService;
import com.example.demo.usecase.AuthUseCase;
import com.example.demo.usecase.ItineraryUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 요청 본문 검증.
 *
 * GlobalExceptionHandler 에는 MethodArgumentNotValidException 처리가 예전부터 있었지만
 * 어떤 컨트롤러도 @Valid 를 붙이지 않아 한 번도 호출될 수 없었다.
 * 그 사이 제목 없는 여행 생성 요청은 엔티티의 NOT NULL 제약에 걸려 500 으로 떨어졌다.
 *
 * 여기서는 잘못된 본문이 서비스 계층에 닿기 전에 400 으로 막히는지,
 * 그리고 어느 필드가 왜 틀렸는지 응답에 담기는지를 확인한다.
 */
@ExtendWith(MockitoExtension.class)
class RequestValidationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TripService tripService;

    @Mock
    private AuthUseCase authService;

    @Mock
    private ItineraryUseCase itineraryService;

    @Mock
    private TripReviewService tripReviewService;

    private MockMvc mockMvc(Object controller) {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();
    }

    @Test
    @DisplayName("제목 없는 여행 생성 요청은 400 으로 막히고 서비스까지 내려가지 않는다")
    void createTrip_BlankTitle_RejectedBeforeService() throws Exception {
        TripRequest request = TripRequest.builder().title("  ").destination("제주").build();

        mockMvc(new TripController(tripService))
                .perform(post("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("title")));

        verify(tripService, never()).createTrip(any());
    }

    @Test
    @DisplayName("형식이 아닌 이메일로는 가입할 수 없다")
    void signup_MalformedEmail_Rejected() throws Exception {
        AuthRequest.SignupRequest request = new AuthRequest.SignupRequest();
        request.setName("주희");
        request.setEmail("not-an-email");
        request.setPassword("password123");

        mockMvc(new AuthController(authService))
                .perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("email")));

        verify(authService, never()).signup(any());
    }

    @Test
    @DisplayName("너무 짧은 비밀번호로는 가입할 수 없다")
    void signup_ShortPassword_Rejected() throws Exception {
        AuthRequest.SignupRequest request = new AuthRequest.SignupRequest();
        request.setName("주희");
        request.setEmail("juhee@example.com");
        request.setPassword("123");

        mockMvc(new AuthController(authService))
                .perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("password")));

        verify(authService, never()).signup(any());
    }

    @Test
    @DisplayName("올바른 본문은 그대로 통과한다")
    void createTrip_ValidRequest_PassesThrough() throws Exception {
        TripRequest request = TripRequest.builder().title("제주 여행").destination("제주").build();

        mockMvc(new TripController(tripService))
                .perform(post("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(tripService).createTrip(any());
    }

    @Test
    @DisplayName("일정 목록 안의 잘못된 항목까지 함께 검증한다")
    void createItinerary_InvalidNestedRoutePoint_Rejected() throws Exception {
        // 코스 자체는 멀쩡하지만 두 번째 일정의 이름이 비어 있다.
        ItineraryRequest request = ItineraryRequest.builder()
                .title("제주 3박 4일")
                .routePoints(List.of(
                        RoutePointRequest.builder().label("성산일출봉").dayNumber(1).sequenceOrder(0).build(),
                        RoutePointRequest.builder().label("").dayNumber(1).sequenceOrder(1).build()))
                .build();

        mockMvc(new ItineraryController(itineraryService))
                .perform(post("/api/itineraries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString("routePoints[1].label")));

        verify(itineraryService, never()).createItinerary(any());
    }

    @Test
    @DisplayName("평점에 숫자가 아닌 값이 오면 500 이 아니라 400 으로 답한다")
    void createReview_NonNumericRating_IsBadRequestNotServerError() throws Exception {
        // 예전에는 Map 본문을 (Number) 로 형변환해 ClassCastException -> 500 이 났다.
        String body = "{\"content\":\"좋았어요\",\"rating\":\"다섯\"}";

        mockMvc(new TripReviewController(tripReviewService))
                .perform(post("/api/trips/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(tripReviewService, never()).createReview(any(), any(), org.mockito.ArgumentMatchers.anyInt(), any(), any());
    }

    @Test
    @DisplayName("평점을 생략하면 기본 5점으로 넘어간다")
    void createReview_MissingRating_DefaultsToFive() throws Exception {
        mockMvc(new TripReviewController(tripReviewService))
                .perform(post("/api/trips/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"좋았어요\"}"))
                .andExpect(status().isOk());

        verify(tripReviewService).createReview(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq("좋았어요"),
                org.mockito.ArgumentMatchers.eq(5),
                org.mockito.ArgumentMatchers.eq("관광지"),
                org.mockito.ArgumentMatchers.isNull());
    }
}
