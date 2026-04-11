package com.After_Buy.NotificationService.controller;

import com.After_Buy.NotificationService.dto.request.FcmTokenUpdateRequest;
import com.After_Buy.NotificationService.dto.response.ApiResponse;
import com.After_Buy.NotificationService.dto.response.PushSettingsResponse;
import com.After_Buy.NotificationService.security.UserPrincipal;
import com.After_Buy.NotificationService.service.PushSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 푸시 설정 컨트롤러 (JWT 인증 필수)
 * 사용자 앱의 푸시 알림 설정 조회 및 FCM 토큰 갱신 요청을 처리합니다.
 * 두 엔드포인트 모두 Lazy Initialization이 적용되어 있습니다.
 *
 * @since   : 2026.04.11
 * @version : 1.0.0
 * @author  : 신태훈
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications/settings")
@RequiredArgsConstructor
@Tag(name = "Push Settings", description = "푸시 알림 설정 조회 및 FCM 토큰 갱신 API")
public class PushSettingsController {

	private final PushSettingsService pushSettingsService;

	/**
	 * 푸시 설정 조회 - Lazy Init 적용
	 * push_settings 레코드가 없으면 기본값(push_enabled=1, fcm_token=null)으로 자동 생성 후 반환합니다.
	 *
	 * @param principal : JWT 인증 정보 (userId 포함)
	 * @return          : 사용자 푸시 설정 응답 DTO
	 */
	@Operation(summary = "푸시 설정 조회", description = "현재 사용자의 푸시 알림 설정을 조회합니다. 설정이 없으면 기본값으로 자동 생성됩니다.",
		security = @SecurityRequirement(name = "bearerAuth"))
	@GetMapping
	public ResponseEntity<ApiResponse<PushSettingsResponse>> getPushSettings(
		@AuthenticationPrincipal UserPrincipal principal
	) {
		Long userId = principal.getUserId();
		log.debug("푸시 설정 조회 요청: userId={}", userId);
		PushSettingsResponse response = pushSettingsService.getOrCreatePushSettings(userId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * FCM 토큰 갱신 (UPSERT) - Lazy Init 복구 트리거 포함
	 * push_settings 레코드가 없으면 Lazy Init으로 먼저 생성한 뒤 토큰을 갱신합니다.
	 * 비동기 init 실패 시 이 엔드포인트를 통해 복구됩니다.
	 *
	 * @param principal : JWT 인증 정보 (userId 포함)
	 * @param request   : 새로운 FCM 토큰 요청 DTO
	 * @return          : 갱신된 푸시 설정 응답 DTO
	 */
	@Operation(summary = "FCM 토큰 갱신", description = "앱에서 발급받은 새로운 FCM 토큰을 서버에 등록/갱신합니다. push_settings 미존재 시 자동 생성됩니다.",
		security = @SecurityRequirement(name = "bearerAuth"))
	@PatchMapping
	public ResponseEntity<ApiResponse<PushSettingsResponse>> updateFcmToken(
		@AuthenticationPrincipal UserPrincipal principal,
		@RequestBody @Valid FcmTokenUpdateRequest request
	) {
		Long userId = principal.getUserId();
		log.debug("FCM 토큰 갱신 요청: userId={}", userId);
		PushSettingsResponse response = pushSettingsService.updateFcmToken(userId, request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
