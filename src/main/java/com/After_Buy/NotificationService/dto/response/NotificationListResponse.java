package com.After_Buy.NotificationService.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 알림 목록 응답 DTO
 * GET /api/notifications 응답의 최상위 래퍼로, 목록과 전체 개수를 함께 반환합니다.
 *
 * @since   : 2026.04.11
 * @version : 1.0.0
 * @author  : 신태훈
 */
@Getter
@Builder
public class NotificationListResponse {

	/** 알림 목록 (유효한 알림만 포함 — auto_delete_at 필터링 적용) */
	private List<NotificationResponse> notifications;

	/** 전체 유효 알림 개수 */
	private int totalCount;

	/**
	 * 알림 목록으로부터 NotificationListResponse를 생성하는 정적 팩토리 메서드
	 *
	 * @param notifications : NotificationResponse 목록
	 * @return              : 총 개수가 포함된 NotificationListResponse DTO
	 */
	public static NotificationListResponse of(List<NotificationResponse> notifications) {
		return NotificationListResponse.builder()
			.notifications(notifications)
			.totalCount(notifications.size())
			.build();
	}
}
