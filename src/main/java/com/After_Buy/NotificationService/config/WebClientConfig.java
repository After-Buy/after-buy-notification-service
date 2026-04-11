package com.After_Buy.NotificationService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 마이크로서비스 간 비동기 HTTP 통신을 위한 WebClient 빈 등록 설정 클래스
 * Device Service 등 내부 서비스 호출 시 InternalDeviceClient 등에서 주입받아 사용합니다.
 *
 * @since   : 2026.04.11
 * @version : 1.0.0
 * @author  : 신태훈
 */
@Configuration
public class WebClientConfig {

	/**
	 * WebClient.Builder 빈 등록 메서드
	 * 각 클라이언트 클래스에서 baseUrl 등을 덮어씌워 사용할 수 있도록 빌더 형태로 제공합니다.
	 *
	 * @return Spring WebFlux WebClient 빌더 인스턴스
	 */
	@Bean
	public WebClient.Builder webClientBuilder() {
		return WebClient.builder();
	}
}
