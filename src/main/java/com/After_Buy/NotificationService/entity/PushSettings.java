package com.After_Buy.NotificationService.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Notification Service - push_settings 테이블 매핑 엔티티
 * 사용자별 FCM 토큰 및 푸시 수신 동의 여부를 저장합니다.
 * user_id는 1인 1설정(UNIQUE)이며, Auth Service users.user_id 논리 참조 방식입니다.
 *
 * @since   : 2026.04.11
 * @version : 1.0.0
 * @author  : 신태훈
 */
@Entity
@Table(
	name = "push_settings",
	indexes = {
		@Index(name = "idx_push_settings_user_id", columnList = "user_id")
	},
	uniqueConstraints = {
		@UniqueConstraint(name = "uq_push_settings_user_id", columnNames = "user_id")
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class PushSettings {

	/**
	 * 설정 고유 ID (PK, AUTO_INCREMENT)
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "setting_id")
	private Long settingId;

	/**
	 * 사용자 ID (Auth Service users.user_id 논리 참조, UNIQUE)
	 */
	@Column(name = "user_id", nullable = false, unique = true)
	private Long userId;

	/**
	 * 푸시 수신 동의 여부 (0: 거부, 1: 동의)
	 * 기본값 1 (동의 상태로 초기 생성)
	 */
	@Column(name = "push_enabled", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
	@Builder.Default
	private int pushEnabled = 1;

	/**
	 * FCM 디바이스 토큰 (NULL 허용 — 앱 미실행 시 없을 수 있음)
	 */
	@Column(name = "fcm_token", length = 300)
	private String fcmToken;

	/**
	 * 마지막 변경 일시 (push_enabled, fcm_token 갱신 시 자동 업데이트)
	 */
	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	/**
	 * FCM 토큰 갱신 도메인 메서드
	 * PATCH /api/notifications/settings 호출 시 새로운 토큰으로 교체합니다.
	 *
	 * @param fcmToken : 클라이언트 앱에서 발급받은 최신 FCM 등록 토큰
	 */
	public void updateFcmToken(String fcmToken) {
		this.fcmToken = fcmToken;
	}

	/**
	 * 푸시 수신 동의 여부 동기화 도메인 메서드
	 * Auth Service의 POST /internal/push-settings/sync 수신 시 사용합니다.
	 *
	 * @param pushEnabled : Auth Service에서 동기화된 push_enabled 값 (0 or 1)
	 */
	public void syncPushEnabled(int pushEnabled) {
		this.pushEnabled = pushEnabled;
	}
}
