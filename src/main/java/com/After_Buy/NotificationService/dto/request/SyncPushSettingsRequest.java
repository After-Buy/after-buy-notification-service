package com.After_Buy.NotificationService.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 푸시 수신 동의 여부 동기화 요청 DTO (내부 API 전용)
 * POST /internal/push-settings/sync 수신 시 Auth Service가 전달하는 요청 본문입니다.
 * 사용자가 Auth Service에서 push_enabled 설정을 변경했을 때 동기화를 위해 호출됩니다.
 *
 * @since   : 2026.04.11
 * @version : 1.0.0
 * @author  : 신태훈
 */
@Getter
@NoArgsConstructor
public class SyncPushSettingsRequest {

	/**
	 * 동기화 대상 사용자 ID
	 */
	@NotNull(message = "userId는 필수입니다.")
	@JsonProperty("user_id")
	private Long userId;

	/**
	 * 변경된 푸시 수신 동의 여부 (0: 거부, 1: 동의)
	 * push_settings 테이블의 push_enabled 컬럼과 1:1 동기화됩니다.
	 */
	@NotNull(message = "pushEnabled는 필수입니다.")
	@Min(value = 0, message = "pushEnabled는 0 또는 1이어야 합니다.")
	@Max(value = 1, message = "pushEnabled는 0 또는 1이어야 합니다.")
	@JsonProperty("push_enabled")
	private Integer pushEnabled;
}
