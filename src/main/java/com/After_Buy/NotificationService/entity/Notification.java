package com.After_Buy.NotificationService.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Notification Service - notifications 테이블 매핑 엔티티
 * 보증기간 알림 이력을 저장하며, auto_delete_at 기준으로 스케줄러가 자동 삭제 처리합니다.
 * device_id, user_id는 논리 참조 방식(FK 없음)으로 관리됩니다.
 *
 * @since   : 2026.04.11
 * @version : 1.0.0
 * @author  : 신태훈
 */
@Entity
@Table(
	name = "notifications",
	indexes = {
		@Index(name = "idx_notifications_user_id",       columnList = "user_id"),
		@Index(name = "idx_notifications_device_id",     columnList = "device_id"),
		@Index(name = "idx_notifications_auto_delete_at", columnList = "auto_delete_at")
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Notification {

	/**
	 * 알림 고유 ID (PK, AUTO_INCREMENT)
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "notification_id")
	private Long notificationId;

	/**
	 * 수신 사용자 ID (Auth Service users.user_id 논리 참조)
	 */
	@Column(name = "user_id", nullable = false)
	private Long userId;

	/**
	 * 대상 기기 ID (Device Service devices.device_id 논리 참조)
	 */
	@Column(name = "device_id", nullable = false)
	private Long deviceId;

	/**
	 * 기기명 스냅샷 (알림 발송 시점의 이름을 저장)
	 */
	@Column(name = "device_name", nullable = false, length = 200)
	private String deviceName;

	/**
	 * 기기 이미지 URL 스냅샷 (NULL 허용)
	 */
	@Column(name = "device_image_url", length = 500)
	private String deviceImageUrl;

	/**
	 * 알림 유형 Enum: WARRANTY_D30 / WARRANTY_D14 / WARRANTY_D1 / WARRANTY_EXPIRED
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "notification_type", nullable = false)
	private NotificationType notificationType;

	/**
	 * 읽음 여부 (0: 미읽음, 1: 읽음)
	 */
	@Column(name = "is_read", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
	@Builder.Default
	private int isRead = 0;

	/**
	 * 보증 만료일 스냅샷 (D-day 계산용)
	 */
	@Column(name = "warranty_expiry_date", nullable = false)
	private LocalDate warrantyExpiryDate;

	/**
	 * 알림 발송 일시
	 */
	@CreationTimestamp
	@Column(name = "sent_at", nullable = false, updatable = false)
	private LocalDateTime sentAt;

	/**
	 * 자동 삭제 예정 일시 (warranty_expiry_date + 7일)
	 * NULL이면 자동 삭제 대상 아님
	 */
	@Column(name = "auto_delete_at")
	private LocalDateTime autoDeleteAt;

	/**
	 * 알림 읽음 처리 도메인 메서드
	 * isRead 값을 1로 변경하여 읽음 상태로 전환합니다.
	 */
	public void markAsRead() {
		this.isRead = 1;
	}

	/**
	 * 알림 유형 Enum 정의 (notifications.notification_type 컬럼과 1:1 매핑)
	 */
	public enum NotificationType {
		/** 보증 만료 30일 전 알림 */
		WARRANTY_D30,
		/** 보증 만료 14일 전 알림 */
		WARRANTY_D14,
		/** 보증 만료 1일 전 알림 */
		WARRANTY_D1,
		/** 보증 만료 당일 알림 */
		WARRANTY_EXPIRED
	}
}
