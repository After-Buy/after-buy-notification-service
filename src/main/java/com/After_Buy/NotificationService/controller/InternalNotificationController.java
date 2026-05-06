package com.After_Buy.NotificationService.controller;

import com.After_Buy.NotificationService.dto.request.BroadcastPushRequest;
import com.After_Buy.NotificationService.dto.request.InitPushSettingsRequest;
import com.After_Buy.NotificationService.dto.request.SyncPushSettingsRequest;
import com.After_Buy.NotificationService.dto.response.ApiResponse;
import com.After_Buy.NotificationService.dto.response.BroadcastResultResponse;
import com.After_Buy.NotificationService.entity.PushSettings;
import com.After_Buy.NotificationService.repository.PushSettingsRepository;
import com.After_Buy.NotificationService.service.FcmPushService;
import com.After_Buy.NotificationService.service.NotificationService;
import com.After_Buy.NotificationService.service.PushSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 내부 API 컨트롤러 (X-Internal-Secret 전용)
 * Auth Service 및 Admin Service의 내부 호출을 처리합니다.
 * SecurityConfig에 의해 /internal/** 경로는 InternalSecretFilter 검증 후 접근됩니다.
 *
 * @since   : 2026.04.11
 * @version : 1.0.0
 * @author  : 신태훈
 */
@Slf4j
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@Tag(name = "Internal API", description = "MSA 내부 서비스 간 통신 전용 API (X-Internal-Secret 필수)")
public class InternalNotificationController {

	private final PushSettingsService pushSettingsService;
	private final NotificationService notificationService;
	private final FcmPushService fcmPushService;
	private final PushSettingsRepository pushSettingsRepository;

	/**
	 * 신규 가입 사용자 push_settings 초기 생성
	 * Auth Service가 회원 가입 완료 후 비동기(Fire & Forget)로 호출합니다.
	 * 이미 레코드가 존재하면 중복 생성 없이 성공으로 반환합니다.
	 *
	 * @param request : userId가 담긴 초기 생성 요청 DTO
	 * @return        : 성공 메시지
	 */
	@Operation(summary = "push_settings 초기 생성", description = "신규 가입 사용자의 pushSettings 레코드를 생성합니다. (Auth Service 비동기 호출)")
	@PostMapping("/push-settings/init")
	public ResponseEntity<ApiResponse<Void>> initPushSettings(
		@RequestBody @Valid InitPushSettingsRequest request
	) {
		log.info("내부 API — push_settings 초기 생성 요청: userId={}", request.getUserId());
		pushSettingsService.initPushSettings(request);
		return ResponseEntity.ok(ApiResponse.successMessage("push_settings 초기 생성 완료"));
	}

	/**
	 * push_enabled 동기화
	 * Auth Service에서 사용자가 push_enabled를 변경했을 때 동기 호출합니다.
	 *
	 * @param request : userId + pushEnabled 값이 담긴 동기화 요청 DTO
	 * @return        : 성공 메시지
	 */
	@Operation(summary = "push_enabled 동기화", description = "Auth Service에서 변경된 push_enabled 값을 push_settings에 동기화합니다.")
	@PostMapping("/push-settings/sync")
	public ResponseEntity<ApiResponse<Void>> syncPushEnabled(
		@RequestBody @Valid SyncPushSettingsRequest request
	) {
		log.info("내부 API — push_enabled 동기화 요청: userId={}, pushEnabled={}", request.getUserId(), request.getPushEnabled());
		pushSettingsService.syncPushEnabled(request);
		return ResponseEntity.ok(ApiResponse.successMessage("push_enabled 동기화 완료"));
	}

	/**
	 * 회원 탈퇴 시 알림 데이터 전체 삭제
	 * Auth Service에서 탈퇴 처리 완료 후 비동기로 호출합니다.
	 * notifications + push_settings 모두 삭제합니다.
	 *
	 * @param userId : 탈퇴 처리 대상 사용자 ID (PathVariable)
	 * @return       : 성공 메시지
	 */
	@Operation(summary = "탈퇴 사용자 알림 전체 삭제", description = "회원 탈퇴 시 notifications + push_settings를 일괄 삭제합니다.")
	@DeleteMapping("/notifications/users/{userId}")
	public ResponseEntity<ApiResponse<Void>> deleteUserNotifications(
		@PathVariable Long userId
	) {
		log.info("내부 API — 탈퇴 사용자 알림 삭제 요청: userId={}", userId);
		notificationService.deleteUserAllData(userId);
		return ResponseEntity.ok(ApiResponse.successMessage("탈퇴 사용자 알림 데이터 삭제 완료"));
	}

	/**
	 * 전체 사용자 FCM 브로드캐스트 발송
	 * Admin Service에서 공지성 푸시 알림 발송 시 호출합니다.
	 * push_enabled=1 이고 fcm_token이 존재하는 사용자에게만 발송합니다.
	 *
	 * @param request : 브로드캐스트 제목 및 본문
	 * @return        : 전체/성공/실패 건수가 담긴 결과 DTO
	 */
	@Operation(summary = "전체 FCM 브로드캐스트", description = "push_enabled=1 이고 토큰이 존재하는 모든 사용자에게 FCM을 발송합니다.")
	@PostMapping("/push/broadcast")
	public ResponseEntity<ApiResponse<BroadcastResultResponse>> broadcast(
		@RequestBody @Valid BroadcastPushRequest request
	) {
		log.info("내부 API — 브로드캐스트 발송 요청: title={}", request.getTitle());

		List<PushSettings> targets = pushSettingsRepository.findAllEnabledWithToken();
		int total = targets.size();
		int success = 0;
		int fail = 0;

		for (PushSettings ps : targets) {
			boolean sent = fcmPushService.sendBroadcastPush(
				ps.getFcmToken(),
				request.getTitle(),
				request.getBody(),
				request.getDeepLink(),
				request.getAnnouncementId()
			);
			if (sent) success++;
			else fail++;
		}

		BroadcastResultResponse result = BroadcastResultResponse.of(total, success, fail);
		log.info("브로드캐스트 완료: total={}, success={}, fail={}", total, success, fail);
		return ResponseEntity.ok(ApiResponse.success(result));
	}
}

