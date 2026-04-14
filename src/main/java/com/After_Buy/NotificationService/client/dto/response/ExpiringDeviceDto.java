package com.After_Buy.NotificationService.client.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Device Service 내부 API 응답 개별 기기 DTO
 * Device Service의 ExpiringDeviceDto와 동일한 JSON 필드명(snake_case)으로 역직렬화합니다.
 * 명세서(API명세서_Part5_Internal_딥링크_부록.md)의 JSON 구조 및
 * DB 명세서(논리적 DB 설계)의 컬럼명을 기준으로 매핑합니다.
 *
 * @since : 2026.04.14
 * @version : 1.0.0
 * @author : 최준혁
 */
@Getter
@NoArgsConstructor
public class ExpiringDeviceDto {

	@JsonProperty("device_id")
	private Long deviceId;

	@JsonProperty("user_id")
	private Long userId;

	@JsonProperty("product_name")
	private String productName;

	@JsonProperty("image_url")
	private String imageUrl;

	@JsonProperty("warranty_expiry_date")
	private String warrantyExpiryDate;
}
