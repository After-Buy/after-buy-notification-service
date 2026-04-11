package com.After_Buy.NotificationService.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 푸시 설정 초기 생성 요청 DTO (내부 API 전용)
 * POST /internal/push-settings/init 수신 시 Auth Service가 전달하는 요청 본문입니다.
 * 신규 회원 가입 시 Auth Service가 비동기(Fire & Forget)로 호출합니다.
 *
 * @since   : 2026.04.11
 * @version : 1.0.0
 * @author  : 신태훈
 */
@Getter
@NoArgsConstructor
public class InitPushSettingsRequest {

	/**
	 * 신규 가입 사용자 ID (Auth Service users.user_id)
	 * push_settings 레코드 초기 생성의 기준 키입니다.
	 */
	@NotNull(message = "userId는 필수입니다.")
	private Long userId;
}
