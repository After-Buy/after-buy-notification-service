package com.After_Buy.NotificationService.service;

import com.After_Buy.NotificationService.dto.request.FcmTokenUpdateRequest;
import com.After_Buy.NotificationService.dto.request.InitPushSettingsRequest;
import com.After_Buy.NotificationService.dto.request.SyncPushSettingsRequest;
import com.After_Buy.NotificationService.dto.response.PushSettingsResponse;
import com.After_Buy.NotificationService.entity.PushSettings;
import com.After_Buy.NotificationService.repository.PushSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 푸시 설정 비즈니스 로직 서비스 클래스
 * Lazy Initialization 패턴의 핵심 구현체로,
 * Auth Service 비동기 init 실패 시에도 최초 접근 시점에 push_settings 레코드를 자동 생성합니다.
 *
 * @since   : 2026.04.11
 * @version : 1.0.0
 * @author  : 신태훈
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushSettingsService {

	private final PushSettingsRepository pushSettingsRepository;

	/**
	 * 푸시 설정 조회 - Lazy Initialization 핵심 메서드
	 * 사용자의 push_settings 레코드가 없는 경우 기본값(push_enabled=1, fcm_token=null)으로 자동 생성합니다.
	 * GET /api/notifications/settings 의 Lazy Init 트리거 포인트입니다.
	 *
	 * @param userId : 조회 대상 사용자 ID
	 * @return       : 기존 또는 새로 생성된 PushSettingsResponse
	 */
	@Transactional
	public PushSettingsResponse getOrCreatePushSettings(Long userId) {
		PushSettings pushSettings = pushSettingsRepository.findByUserId(userId)
			.orElseGet(() -> {
				log.info("push_settings 레코드 없음 — Lazy Init 생성: userId={}", userId);
				return pushSettingsRepository.save(
					PushSettings.builder()
						.userId(userId)
						.pushEnabled(1)
						.fcmToken(null)
						.build()
				);
			});
		return PushSettingsResponse.from(pushSettings);
	}

	/**
	 * FCM 토큰 갱신 (UPSERT 방식) - Lazy Init 복구 트리거 포함
	 * push_settings 레코드가 없으면 Lazy Init으로 먼저 생성한 뒤 토큰을 갱신합니다.
	 * PATCH /api/notifications/settings 처리 메서드입니다.
	 *
	 * @param userId  : 갱신 대상 사용자 ID
	 * @param request : 새로운 FCM 토큰이 담긴 요청 DTO
	 * @return        : 갱신된 PushSettingsResponse
	 * @throws com.After_Buy.NotificationService.exception.CustomException : 유효하지 않은 요청 시 PUSH_SETTINGS_INVALID
	 */
	@Transactional
	public PushSettingsResponse updateFcmToken(Long userId, FcmTokenUpdateRequest request) {
		PushSettings pushSettings = pushSettingsRepository.findByUserId(userId)
			.orElseGet(() -> {
				log.info("FCM 토큰 갱신 시 push_settings 없음 — Lazy Init 생성 후 토큰 설정: userId={}", userId);
				return pushSettingsRepository.save(
					PushSettings.builder()
						.userId(userId)
						.pushEnabled(1)
						.fcmToken(null)
						.build()
				);
			});
		pushSettings.updateFcmToken(request.getFcmToken());
		log.debug("FCM 토큰 갱신 완료: userId={}", userId);
		return PushSettingsResponse.from(pushSettings);
	}

	/**
	 * push_settings 초기 생성 - 내부 API 전용 (Auth Service 비동기 호출)
	 * 이미 레코드가 존재하면 중복 생성 없이 기존 레코드를 반환합니다.
	 * POST /internal/push-settings/init 처리 메서드입니다.
	 *
	 * @param request : userId가 담긴 초기 생성 요청 DTO
	 */
	@Transactional
	public void initPushSettings(InitPushSettingsRequest request) {
		boolean exists = pushSettingsRepository.findByUserId(request.getUserId()).isPresent();
		if (exists) {
			log.info("push_settings 이미 존재 — init 건너뜀: userId={}", request.getUserId());
			return;
		}
		pushSettingsRepository.save(
			PushSettings.builder()
				.userId(request.getUserId())
				.pushEnabled(1)
				.fcmToken(null)
				.build()
		);
		log.info("push_settings 초기 생성 완료: userId={}", request.getUserId());
	}

	/**
	 * push_enabled 동기화 - 내부 API 전용 (Auth Service 동기 호출)
	 * push_settings 레코드가 없으면 Lazy Init으로 먼저 생성합니다.
	 * POST /internal/push-settings/sync 처리 메서드입니다.
	 *
	 * @param request : userId 및 변경된 pushEnabled 값이 담긴 동기화 요청 DTO
	 */
	@Transactional
	public void syncPushEnabled(SyncPushSettingsRequest request) {
		PushSettings pushSettings = pushSettingsRepository.findByUserId(request.getUserId())
			.orElseGet(() -> {
				log.info("push_settings 없음 — sync 시 Lazy Init 생성: userId={}", request.getUserId());
				return pushSettingsRepository.save(
					PushSettings.builder()
						.userId(request.getUserId())
						.pushEnabled(1)
						.fcmToken(null)
						.build()
				);
			});
		pushSettings.syncPushEnabled(request.getPushEnabled());
		log.debug("push_enabled 동기화 완료: userId={}, pushEnabled={}", request.getUserId(), request.getPushEnabled());
	}

	/**
	 * 회원 탈퇴 시 push_settings 삭제 - 내부 API 전용
	 * NotificationService.deleteUserAllData()에서 트랜잭션 내 함께 호출됩니다.
	 *
	 * @param userId : 탈퇴 처리 대상 사용자 ID
	 */
	@Transactional
	public void deletePushSettings(Long userId) {
		pushSettingsRepository.deleteByUserId(userId);
		log.info("push_settings 삭제 완료: userId={}", userId);
	}
}
