package com.After_Buy.NotificationService.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 전체 사용자 FCM 브로드캐스트 발송 요청 DTO (내부 API 전용)
 * POST /internal/push/broadcast 수신 시 Admin Service가 전달하는 요청 본문입니다.
 * push_enabled=1 이고 fcm_token이 존재하는 모든 사용자에게 FCM을 발송합니다.
 *
 * @since   : 2026.04.11
 * @version : 1.1.0
 * @author  : 신태훈
 */
@Getter
@NoArgsConstructor
public class BroadcastPushRequest {

	/**
	 * FCM 푸시 알림 제목
	 * 공백 불가, 최대 100자 제한
	 */
	@NotBlank(message = "알림 제목은 필수입니다.")
	@Size(max = 100, message = "알림 제목은 100자를 초과할 수 없습니다.")
	private String title;

	/**
	 * FCM 푸시 알림 본문 내용
	 * 공백 불가, 최대 500자 제한
	 */
	@NotBlank(message = "알림 내용은 필수입니다.")
	@Size(max = 500, message = "알림 내용은 500자를 초과할 수 없습니다.")
	private String body;

	/**
	 * 앱 내 딥링크 URL (선택값)
	 * Admin Service가 공지사항 등록 시 전달하는 앱 내 이동 경로입니다.
	 * 예: "afterbuy://announcements/1"
	 * null인 경우 FCM data payload에 포함하지 않습니다.
	 */
	@JsonProperty("deep_link")
	private String deepLink;

	/**
	 * 공지사항 ID (선택값)
	 * Admin Service가 공지사항 등록 시 전달하는 공지사항 식별자입니다.
	 * null인 경우 FCM data payload에 포함하지 않습니다.
	 */
	@JsonProperty("announcement_id")
	private Long announcementId;
}
