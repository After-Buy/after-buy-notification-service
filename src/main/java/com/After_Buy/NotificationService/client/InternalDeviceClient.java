package com.After_Buy.NotificationService.client;

import com.After_Buy.NotificationService.client.dto.response.ExpiringDeviceDto;
import com.After_Buy.NotificationService.client.dto.response.InternalWarrantyExpiringResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Device Service 내부 API WebClient 클라이언트
 * 보증 만료 임박 기기 목록을 Device Service로부터 동기 조회합니다.
 * 스케줄러(WarrantyAlertScheduler)에서 매일 09:00에 호출합니다.
 * 응답의 JSON 구조({ "devices": [...] })에 맞게 InternalWarrantyExpiringResponse DTO로 역직렬화합니다.
 *
 * @since   : 2026.04.11
 * @version : 1.1.0
 * @author  : 신태훈
 */
@Slf4j
@Component
public class InternalDeviceClient {

	private final WebClient webClient;
	private final String internalSecretKey;

	public InternalDeviceClient(
		WebClient.Builder webClientBuilder,
		@Value("${services.device-url}") String deviceUrl,
		@Value("${internal.secret-key}") String internalSecretKey
	) {
		this.webClient = webClientBuilder
			.baseUrl(deviceUrl)
			.build();
		this.internalSecretKey = internalSecretKey;
	}

	/**
	 * 보증 만료 임박 기기 목록 조회 - Device Service 내부 API 호출
	 * GET /internal/devices/warranty-expiring?days={days} 를 호출합니다.
	 * 발송 실패 시 빈 리스트를 반환하여 스케줄러 전체 중단을 방지합니다.
	 *
	 * @param days : 만료 임박 기준 일수 (1, 14, 30 등)
	 * @return     : 보증 만료 임박 기기 정보 목록 (ExpiringDeviceDto 타입)
	 */
	public List<ExpiringDeviceDto> getWarrantyExpiringDevices(int days) {
		try {
			InternalWarrantyExpiringResponse result = webClient.get()
				.uri(uriBuilder -> uriBuilder
					.path("/internal/devices/warranty-expiring")
					.queryParam("days", days)
					.build())
				.header("X-Internal-Secret", internalSecretKey)
				.retrieve()
				.bodyToMono(InternalWarrantyExpiringResponse.class)
				.onErrorResume(WebClientResponseException.class, e -> {
					log.error("Device Service 응답 오류: days={}, status={}, message={}",
						days, e.getStatusCode(), e.getMessage());
					return Mono.empty();
				})
				.block();

			if (result == null || result.getDevices() == null) return List.of();
			log.debug("Device Service 기기 목록 수신 완료: days={}, count={}", days, result.getDevices().size());
			return result.getDevices();

		} catch (Exception e) {
			log.error("Device Service 호출 실패 (연결 불가 등): days={}, error={}", days, e.getMessage());
			return List.of();
		}
	}
}
