package com.After_Buy.NotificationService.dto.response;

import com.After_Buy.NotificationService.entity.Notification;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 단일 알림 응답 DTO
 * GET /api/notifications 목록 조회 시 각 알림 항목의 응답 데이터로 사용됩니다.
 *
 * @since   : 2026.04.11
 * @version : 1.0.0
 * @author  : 신태훈
 */
@Getter
@Builder
public class NotificationResponse {

	/** 알림 고유 ID */
	private Long notificationId;

	/** 기기 ID */
	private Long deviceId;

	/** 기기명 스냅샷 */
	private String deviceName;

	/** 기기 이미지 URL 스냅샷 (NULL 가능) */
	private String deviceImageUrl;

	/** 알림 유형 문자열 (WARRANTY_D30, WARRANTY_D14, WARRANTY_D1, WARRANTY_EXPIRED) */
	private String notificationType;

	/** 읽음 여부 (0: 미읽음, 1: 읽음) */
	private int isRead;

	/** 보증 만료일 스냅샷 */
	private LocalDate warrantyExpiryDate;

	/** 알림 발송 일시 */
	private LocalDateTime sentAt;

	/**
	 * Notification 엔티티를 NotificationResponse DTO로 변환하는 정적 팩토리 메서드
	 *
	 * @param notification : 변환 대상 Notification 엔티티
	 * @return             : 클라이언트 응답용 NotificationResponse DTO
	 */
	public static NotificationResponse from(Notification notification) {
		return NotificationResponse.builder()
			.notificationId(notification.getNotificationId())
			.deviceId(notification.getDeviceId())
			.deviceName(notification.getDeviceName())
			.deviceImageUrl(notification.getDeviceImageUrl())
			.notificationType(notification.getNotificationType().name())
			.isRead(notification.getIsRead())
			.warrantyExpiryDate(notification.getWarrantyExpiryDate())
			.sentAt(notification.getSentAt())
			.build();
	}
}
