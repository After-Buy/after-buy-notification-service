package com.After_Buy.NotificationService.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger API 명세서 설정을 위한 Configuration 클래스입니다.
 * 
 * JWT(사용자 인증) 및 X-Internal-Secret(MSA 내부 통신 인증) 토큰 입력을 위한
 * 전역 Authorization 버튼 UI를 명세서 상단에 활성화합니다.
 *
 * @author 신태훈
 * @version 1.0
 * @since 2026-04-11
 */
@Configuration
public class SwaggerConfig {
        /**
         * OpenAPI(Swagger) 기본 명세 및 권한 스킴을 정의하는 Bean입니다.
         *
         * @return 전역 인증 헤더가 포함된 OpenAPI 객체
         */
        @Bean
        public OpenAPI openAPI() {
                String jwtSchemeName = "jwtAuth";
                String internalSecretSchemeName = "internalSecretAuth";

                SecurityRequirement securityRequirement = new SecurityRequirement()
                                .addList(jwtSchemeName)
                                .addList(internalSecretSchemeName);

                Components components = new Components()
                                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                                                .name(jwtSchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description("사용자 앱 API 인증용 JWT 토큰 (Bearer 제외)"))
                                .addSecuritySchemes(internalSecretSchemeName, new SecurityScheme()
                                                .name("X-Internal-Secret")
                                                .type(SecurityScheme.Type.APIKEY)
                                                .in(SecurityScheme.In.HEADER)
                                                .description("MSA 내부 통신용 시크릿 키 (/internal/** API)"));

                return new OpenAPI()
                                .info(new Info().title("After-Buy Notification Service API")
                                                .description("알림 서비스 (앱 연동 및 MSA 내부 통신 API)")
                                                .version("v1.0.0"))
                                .addSecurityItem(securityRequirement)
                                .components(components);
        }
}
