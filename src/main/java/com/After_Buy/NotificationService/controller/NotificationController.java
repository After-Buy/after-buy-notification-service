package com.After_Buy.NotificationService.controller;

import com.After_Buy.NotificationService.dto.response.ApiResponse;
import com.After_Buy.NotificationService.dto.response.NotificationListResponse;
import com.After_Buy.NotificationService.security.UserPrincipal;
import com.After_Buy.NotificationService.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 알림 컨트롤러 (JWT 인증 필수)
 * 사용자 앱의 알림 목록 조회, 읽음 처리, 수동 삭제 요청을 처리합니다.
 * 모든 요청은 JWT 인증이 필수이며, 본인 소유 알림에만 접근할 수 있습니다.
 *
 * @since   : 2026.04.11
 * @version : 1.0.0
 * @author  : 신태훈
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "알림 목록 조회, 읽음 처리, 삭제 API")
public class NotificationController {

	private final NotificationService notificationService;

	/**
	 * 알림 목록 조회 - Lazy Init 적용
	 * auto_delete_at 이전(과거)인 알림은 응답에서 제외합니다.
	 * push_settings 레코드가 없으면 기본값으로 자동 생성합니다.
	 *
	 * @param principal : JWT 인증 정보 (userId 포함)
	 * @return          : 유효한 알림 목록과 총 개수
	 */
	@Operation(summary = "알림 목록 조회", description = "유효한 알림 목록을 조회합니다. auto_delete_at 필터링 및 Lazy Init이 적용됩니다.",
		security = @SecurityRequirement(name = "bearerAuth"))
	@GetMapping("/home")
	public ResponseEntity<ApiResponse<NotificationListResponse>> getNotifications(
		@AuthenticationPrincipal UserPrincipal principal
	) {
		Long userId = principal.getUserId();
		log.debug("알림 목록 조회 요청: userId={}", userId);
		NotificationListResponse response = notificationService.getNotifications(userId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 알림 읽음 처리
	 * 본인 소유 알림이 아닌 경우 403 NOTIFICATION_ACCESS_DENIED를 반환합니다.
	 * 존재하지 않는 알림인 경우 404 NOTIFICATION_NOT_FOUND를 반환합니다.
	 *
	 * @param principal      : JWT 인증 정보 (userId 포함)
	 * @param notificationId : 읽음 처리 대상 알림 ID
	 * @return               : 성공 메시지
	 */
	@Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다. 본인 소유 알림만 처리 가능합니다.",
		security = @SecurityRequirement(name = "bearerAuth"))
	@PatchMapping("/{notificationId}/read")
	public ResponseEntity<ApiResponse<Void>> markAsRead(
		@AuthenticationPrincipal UserPrincipal principal,
		@PathVariable Long notificationId
	) {
		Long userId = principal.getUserId();
		log.debug("알림 읽음 처리 요청: userId={}, notificationId={}", userId, notificationId);
		notificationService.markAsRead(userId, notificationId);
		return ResponseEntity.ok(ApiResponse.successMessage("알림 읽음 처리 완료"));
	}

	/**
	 * 알림 수동 삭제
	 * 본인 소유 알림이 아닌 경우 403 NOTIFICATION_ACCESS_DENIED를 반환합니다.
	 * 존재하지 않는 알림인 경우 404 NOTIFICATION_NOT_FOUND를 반환합니다.
	 *
	 * @param principal      : JWT 인증 정보 (userId 포함)
	 * @param notificationId : 삭제 대상 알림 ID
	 * @return               : 성공 메시지
	 */
	@Operation(summary = "알림 수동 삭제", description = "특정 알림을 수동으로 삭제합니다. 본인 소유 알림만 삭제 가능합니다.",
		security = @SecurityRequirement(name = "bearerAuth"))
	@DeleteMapping("/{notificationId}")
	public ResponseEntity<ApiResponse<Void>> deleteNotification(
		@AuthenticationPrincipal UserPrincipal principal,
		@PathVariable Long notificationId
	) {
		Long userId = principal.getUserId();
		log.debug("알림 삭제 요청: userId={}, notificationId={}", userId, notificationId);
		notificationService.deleteNotification(userId, notificationId);
		return ResponseEntity.ok(ApiResponse.successMessage("알림 삭제 완료"));
	}
}
