package com.After_Buy.NotificationService.client.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Device Service 내부 API 응답 래퍼 DTO
 * GET /internal/devices/warranty-expiring 의 최상위 응답 객체입니다.
 * 명세서(API명세서_Part5_Internal_딥링크_부록.md) 기준: { "devices": [ ... ] }
 *
 * @since : 2026.04.14
 * @version : 1.0.0
 * @author : 최준혁
 */
@Getter
@NoArgsConstructor
public class InternalWarrantyExpiringResponse {

	@JsonProperty("devices")
	private List<ExpiringDeviceDto> devices;
}
