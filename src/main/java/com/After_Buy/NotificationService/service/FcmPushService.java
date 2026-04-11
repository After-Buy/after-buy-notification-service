package com.After_Buy.NotificationService.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Firebase Cloud Messaging (FCM) 푸시 알림 발송 서비스 클래스
 * Firebase Admin SDK를 통해 단건 FCM 발송 및 브로드캐스트 지원 로직을 담당합니다.
 * FirebaseConfig에서 초기화된 FirebaseApp을 기반으로 동작합니다.
 *
 * @since   : 2026.04.11
 * @version : 1.0.0
 * @author  : 신태훈
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FcmPushService {

	/**
	 * 단일 FCM 토큰 대상 푸시 알림 발송 메서드
	 * push_enabled=1 이고 fcm_token이 존재하는 사용자에게만 호출해야 합니다.
	 * 발송 실패 시 예외를 던지지 않고 에러 로그만 기록하여 스케줄러 전체 중단을 방지합니다.
	 *
	 * @param fcmToken : 발송 대상 FCM 디바이스 토큰
	 * @param title    : 푸시 알림 제목
	 * @param body     : 푸시 알림 본문
	 * @return         : FCM 발송 성공 여부 (true: 성공, false: 실패)
	 */
	public boolean sendPush(String fcmToken, String title, String body) {
		Message message = Message.builder()
			.setToken(fcmToken)
			.setNotification(
				Notification.builder()
					.setTitle(title)
					.setBody(body)
					.build()
			)
			.build();

		try {
			String response = FirebaseMessaging.getInstance().send(message);
			log.debug("FCM 발송 성공: messageId={}, token={}", response, maskToken(fcmToken));
			return true;
		} catch (FirebaseMessagingException e) {
			log.error("FCM 발송 실패: token={}, errorCode={}, message={}",
				maskToken(fcmToken), e.getErrorCode(), e.getMessage());
			return false;
		}
	}

	/**
	 * 브로드캐스트 — 단건 발송 성공 여부 반환 래퍼 메서드
	 * InternalNotificationController의 브로드캐스트 처리 루프에서 호출합니다.
	 * sendPush()와 동일 로직이나 브로드캐스트 맥락임을 명확히 하기 위해 분리합니다.
	 *
	 * @param fcmToken : 발송 대상 FCM 토큰
	 * @param title    : 브로드캐스트 메시지 제목
	 * @param body     : 브로드캐스트 메시지 본문
	 * @return         : 발송 성공 여부
	 */
	public boolean sendBroadcastPush(String fcmToken, String title, String body) {
		return sendPush(fcmToken, title, body);
	}

	/**
	 * FCM 토큰 마스킹 유틸 메서드 (로그 보안)
	 * 토큰의 앞 10자리만 표시하고 나머지는 *로 가립니다.
	 *
	 * @param token : 원본 FCM 토큰
	 * @return      : 마스킹된 토큰 문자열
	 */
	private String maskToken(String token) {
		if (token == null || token.length() <= 10) return "***";
		return token.substring(0, 10) + "***";
	}
}
