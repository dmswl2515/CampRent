package com.rental.camprent.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    /**
     * OpenAPI 3.0 설정
     * Swagger UI에서 보여질 API 문서 정보를 구성
     * */
    @Bean
    public OpenAPI openAPI() {
        // JWT 인증 스키마 이름
        String jwtSchemeName = "bearerAuth";

        // 1. API 기본 정보(좌측 상단에 표시)
        Info info = new Info()
                .title("CampRent API") // API 제목
                .description("캠핑 장비 렌탈 플랫폼 REST API 문서")  // 설명
                .version("v1.0.0"); // 버전

        // 2. SecurityScheme : JWT 인증 방식 정의
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)     // HTTP 인증
                .scheme("bearer")                   // Bearer 토큰 방식
                .bearerFormat("JWT")                // JWT 형식
                .in(SecurityScheme.In.HEADER)       // Authorization 헤더에 포함
            .name("Authorization");                 //헤더 이름

        // 3. SecurityRequirement: 모든 API에 JWT 인증 적용
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        // 4. OpenAPI 객체 생성 및 반환
        return new OpenAPI()
                .info(info) // API 기본 정보
                .components(new Components()    // 컴포넌트 설정
                        .addSecuritySchemes(jwtSchemeName, securityScheme)) // JWT 스키마 등록
                .addSecurityItem(securityRequirement);  // 전역 보안 요구사항 적용
    }
}
