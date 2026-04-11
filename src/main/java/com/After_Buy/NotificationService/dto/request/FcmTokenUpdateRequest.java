package com.After_Buy.NotificationService.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * FCM 토큰 갱신 요청 DTO
 * PATCH /api/notifications/settings 호출 시 클라이언트가 전달하는 요청 본문입니다.
 * 앱에서 새로운 FCM 토큰을 발급받았을 때 서버에 동기화하거나,
 * 비동기 init 실패 시 복구 트리거로 사용됩니다.
 *
 * @since   : 2026.04.11
 * @version : 1.0.0
 * @author  : 신태훈
 */
@Getter
@NoArgsConstructor
public class FcmTokenUpdateRequest {

	/**
	 * 클라이언트 앱에서 Firebase SDK를 통해 발급받은 FCM 등록 토큰
	 * 공백 불가 — 실제 디바이스 토큰 문자열이어야 합니다.
	 */
	@NotBlank(message = "FCM 토큰은 필수입니다.")
	private String fcmToken;
}
