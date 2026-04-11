package com.After_Buy.NotificationService.service;

import com.After_Buy.NotificationService.dto.response.NotificationListResponse;
import com.After_Buy.NotificationService.dto.response.NotificationResponse;
import com.After_Buy.NotificationService.entity.Notification;
import com.After_Buy.NotificationService.entity.PushSettings;
import com.After_Buy.NotificationService.exception.CustomException;
import com.After_Buy.NotificationService.exception.ErrorCode;
import com.After_Buy.NotificationService.repository.NotificationRepository;
import com.After_Buy.NotificationService.repository.PushSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알림 비즈니스 로직 서비스 클래스
 * 알림 목록 조회(Lazy Init 포함), 읽음 처리, 수동 삭제, 회원 탈퇴 시 데이터 전체 삭제를 담당합니다.
 * auto_delete_at 필터링 및 본인 알림 접근 제어 정책을 적용합니다.
 *
 * @since   : 2026.04.11
 * @version : 1.0.0
 * @author  : 신태훈
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final PushSettingsRepository pushSettingsRepository;

	/**
	 * 사용자 알림 목록 조회 - Lazy Initialization 적용
	 * auto_delete_at이 현재 시각 이전인 알림은 응답에서 제외합니다.
	 * push_settings 레코드가 없으면 기본값으로 자동 생성합니다. (Lazy Init)
	 * GET /api/notifications 처리 메서드입니다.
	 *
	 * @param userId : 조회 대상 사용자 ID (JWT에서 추출)
	 * @return       : 유효한 알림 목록과 총 개수
	 */
	@Transactional
	public NotificationListResponse getNotifications(Long userId) {
		// Lazy Init — push_settings 미존재 시 자동 생성
		pushSettingsRepository.findByUserId(userId)
			.orElseGet(() -> {
				log.info("알림 목록 조회 시 push_settings 없음 — Lazy Init 생성: userId={}", userId);
				return pushSettingsRepository.save(
					PushSettings.builder()
						.userId(userId)
						.pushEnabled(1)
						.fcmToken(null)
						.build()
				);
			});

		List<NotificationResponse> notifications = notificationRepository
			.findActiveByUserId(userId, LocalDateTime.now())
			.stream()
			.map(NotificationResponse::from)
			.toList();

		log.debug("알림 목록 조회 완료: userId={}, count={}", userId, notifications.size());
		return NotificationListResponse.of(notifications);
	}

	/**
	 * 알림 읽음 처리
	 * 본인 소유 알림인지 확인 후 is_read를 1로 변경합니다.
	 * PATCH /api/notifications/{notificationId}/read 처리 메서드입니다.
	 *
	 * @param userId         : 요청 사용자 ID (JWT에서 추출)
	 * @param notificationId : 읽음 처리 대상 알림 ID
	 * @throws CustomException : NOTIFICATION_NOT_FOUND(404) — 존재하지 않는 알림
	 * @throws CustomException : NOTIFICATION_ACCESS_DENIED(403) — 타인 알림 접근 시도
	 */
	@Transactional
	public void markAsRead(Long userId, Long notificationId) {
		Notification notification = notificationRepository.findById(notificationId)
			.orElseThrow(() -> {
				log.warn("읽음 처리 실패 — 알림 없음: notificationId={}", notificationId);
				return new CustomException(
					ErrorCode.NOTIFICATION_NOT_FOUND.getStatus(),
					ErrorCode.NOTIFICATION_NOT_FOUND.getCode(),
					ErrorCode.NOTIFICATION_NOT_FOUND.getMessage()
				);
			});

		if (!notification.getUserId().equals(userId)) {
			log.warn("읽음 처리 실패 — 접근 거부: requestUserId={}, ownerUserId={}",
				userId, notification.getUserId());
			throw new CustomException(
				ErrorCode.NOTIFICATION_ACCESS_DENIED.getStatus(),
				ErrorCode.NOTIFICATION_ACCESS_DENIED.getCode(),
				ErrorCode.NOTIFICATION_ACCESS_DENIED.getMessage()
			);
		}

		notification.markAsRead();
		log.debug("읽음 처리 완료: userId={}, notificationId={}", userId, notificationId);
	}

	/**
	 * 알림 수동 삭제
	 * 본인 소유 알림인지 확인 후 삭제합니다.
	 * DELETE /api/notifications/{notificationId} 처리 메서드입니다.
	 *
	 * @param userId         : 요청 사용자 ID (JWT에서 추출)
	 * @param notificationId : 삭제 대상 알림 ID
	 * @throws CustomException : NOTIFICATION_NOT_FOUND(404) — 존재하지 않는 알림
	 * @throws CustomException : NOTIFICATION_ACCESS_DENIED(403) — 타인 알림 접근 시도
	 */
	@Transactional
	public void deleteNotification(Long userId, Long notificationId) {
		Notification notification = notificationRepository.findById(notificationId)
			.orElseThrow(() -> {
				log.warn("알림 삭제 실패 — 알림 없음: notificationId={}", notificationId);
				return new CustomException(
					ErrorCode.NOTIFICATION_NOT_FOUND.getStatus(),
					ErrorCode.NOTIFICATION_NOT_FOUND.getCode(),
					ErrorCode.NOTIFICATION_NOT_FOUND.getMessage()
				);
			});

		if (!notification.getUserId().equals(userId)) {
			log.warn("알림 삭제 실패 — 접근 거부: requestUserId={}, ownerUserId={}",
				userId, notification.getUserId());
			throw new CustomException(
				ErrorCode.NOTIFICATION_ACCESS_DENIED.getStatus(),
				ErrorCode.NOTIFICATION_ACCESS_DENIED.getCode(),
				ErrorCode.NOTIFICATION_ACCESS_DENIED.getMessage()
			);
		}

		notificationRepository.delete(notification);
		log.debug("알림 삭제 완료: userId={}, notificationId={}", userId, notificationId);
	}

	/**
	 * 회원 탈퇴 시 사용자의 모든 알림 및 푸시 설정 일괄 삭제 - 내부 API 전용
	 * DELETE /internal/notifications/users/{userId} 처리 메서드입니다.
	 * notifications + push_settings 모두 삭제합니다.
	 *
	 * @param userId : 탈퇴 처리 대상 사용자 ID
	 */
	@Transactional
	public void deleteUserAllData(Long userId) {
		notificationRepository.deleteAllByUserId(userId);
		pushSettingsRepository.deleteByUserId(userId);
		log.info("회원 탈퇴 처리 완료 — notifications + push_settings 삭제: userId={}", userId);
	}
}
