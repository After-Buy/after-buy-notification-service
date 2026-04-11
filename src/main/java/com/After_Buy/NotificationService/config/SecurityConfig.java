package com.After_Buy.NotificationService.config;

import com.After_Buy.NotificationService.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 스프링 시큐리티 및 인가 방화벽 코어 구성 파일
 * 각종 인증되지 않은 악의적 접근으로부터 알림 API 엔드포인트를 보호하고
 * CORS 횡단 스크립트 및 세션 무상태 정책(Stateless) 조율을 담당하는 글로벌 필터 박스입니다.
 * /internal/** 경로는 InternalSecretFilter에서 별도 보안 처리되므로 SpSecurity에서 permitAll 처리합니다.
 *
 * @since : 2026.04.11
 * @version : 1.0.0
 * @author : 신태훈
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 통합 서블릿 통행 제어 및 권한 결정 빈
     * CSRF를 끄고 JWT 필터를 최앞단에 장착하며 특수 개방 엔드포인트(Swagger, Health, Internal) 예외를 허용한 뒤
     * 모든 남은 구역을 JWT 인증으로 통제합니다.
     *
     * @param http : 방어 체인을 빌드할 스프링의 HttpSecurity 원시 세팅 도구
     * @return : 각종 예외 정책과 룰이 버무려진 보안 필터 체인 박스 반환
     * @throws Exception : 초기화 중 보안 객체 구성 불발 시 익셉션 강제 보고
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 내부망 API (InternalSecretFilter에서 별도 보안 처리)
                        .requestMatchers("/internal/**").permitAll()
                        // 헬스체크
                        .requestMatchers("/actuator/health").permitAll()
                        // Swagger UI
                        .requestMatchers(
                                "/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs/**", "/api/notifications/swagger-ui/**",
                                "/api/notifications/v3/api-docs/**"
                        ).permitAll()
                        // 나머지 모든 요청은 JWT 인증 필수
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * CORS 횡단 자원 공격 방어 보안 해제 규정록
     * 모든 프론트 헤더와 접근 메소드 규칙을 활짝 열어두어 모바일 및 웹 클라이언트 양방향 환경 모두에서 차단 없이 원활하게 접속하게 만들어주는
     * 정책 객체를 생성합니다.
     *
     * @return : 모든 출처 IP 와 통신 헤더가 수락되도록 구성된 개방형 CORS 소스 집합 빈
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
