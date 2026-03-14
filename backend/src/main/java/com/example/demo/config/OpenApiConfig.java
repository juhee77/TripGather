package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tripGatherOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TripGather API")
                        .description("일정 공유 & 동네 모임 API. 모임(Gathering), 일정(Itinerary), 댓글(Comment), 유저(User) 엔드포인트를 확인하고 테스트할 수 있습니다.")
                        .version("1.0"));
    }
}
