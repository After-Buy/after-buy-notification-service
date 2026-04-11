package com.After_Buy.NotificationService.dto.response;

import com.After_Buy.NotificationService.entity.PushSettings;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 푸시 설정 응답 DTO
 * GET /api/notifications/settings 조회 시 반환되는 사용자 푸시 알림 설정 데이터입니다.
 *
 * @since   : 2026.04.11
 * @version : 1.0.0
 * @author  : 신태훈
 */
@Getter
@Builder
public class PushSettingsResponse {

	/** 설정 고유 ID */
	private Long settingId;

	/** 사용자 ID */
	private Long userId;

	/** 푸시 수신 동의 여부 (0: 거부, 1: 동의) */
	private int pushEnabled;

	/** FCM 디바이스 토큰 (NULL 가능 — 미등록 상태) */
	private String fcmToken;

	/** 마지막 변경 일시 */
	private LocalDateTime updatedAt;

	/**
	 * PushSettings 엔티티를 PushSettingsResponse DTO로 변환하는 정적 팩토리 메서드
	 *
	 * @param pushSettings : 변환 대상 PushSettings 엔티티
	 * @return             : 클라이언트 응답용 PushSettingsResponse DTO
	 */
	public static PushSettingsResponse from(PushSettings pushSettings) {
		return PushSettingsResponse.builder()
			.settingId(pushSettings.getSettingId())
			.userId(pushSettings.getUserId())
			.pushEnabled(pushSettings.getPushEnabled())
			.fcmToken(pushSettings.getFcmToken())
			.updatedAt(pushSettings.getUpdatedAt())
			.build();
	}
}
