package com.After_Buy.NotificationService.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Notification Service 전용 에러 코드 열거형 정의 클래스
 * 서비스 정책 및 공통 오류를 코드 + HTTP 상태 + 메시지 묶음으로 일원화하여 관리합니다.
 * GlobalExceptionHandler 및 CustomException과 연계하여 표준화된 오류 응답을 생성합니다.
 *
 * @since   : 2026.04.11
 * @version : 1.0.0
 * @author  : 신태훈
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// ─────────────────────────────────────────────
	// 알림(NOTI) 도메인 에러
	// ─────────────────────────────────────────────

	/**
	 * NOTI-001: 본인의 알림에만 접근 가능 (403)
	 * 타 사용자의 notification_id에 PATCH/DELETE 시도 시 발생
	 */
	NOTIFICATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "NOTI-001", "본인의 알림에만 접근할 수 있습니다."),

	/**
	 * NOTI-002: 존재하지 않는 알림 ID (404)
	 * 삭제되었거나 없는 notification_id 접근 시 발생
	 */
	NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTI-002", "존재하지 않는 알림입니다."),

	/**
	 * NOTI-003: 유효하지 않은 푸시 설정 요청 (400)
	 * FCM 토큰 갱신 등 푸시 설정 관련 입력값 오류 시 발생
	 */
	PUSH_SETTINGS_INVALID(HttpStatus.BAD_REQUEST, "NOTI-003", "유효하지 않은 푸시 설정 요청입니다."),

	// ─────────────────────────────────────────────
	// 공통(COMMON) 에러
	// ─────────────────────────────────────────────

	/**
	 * COMMON-001: 입력값 유효성 검사 실패 (400)
	 * @Valid 어노테이션 등 검증 실패 시 발생
	 */
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON-001", "입력값이 올바르지 않습니다."),

	/**
	 * COMMON-500: 서버 내부 미처리 오류 (500)
	 * 예기치 못한 시스템 예외 발생 시 GlobalExceptionHandler 최후 방어선으로 사용
	 */
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-500", "서버 내부 오류가 발생했습니다.");

	// HTTP 상태 코드
	private final HttpStatus status;

	// 프론트엔드 분기 제어 및 로그 추적용 고유 에러 코드 문자열
	private final String code;

	// 클라이언트에 노출되는 사람이 읽을 수 있는 오류 메시지
	private final String message;
}
