package com.After_Buy.NotificationService.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 브로드캐스트 FCM 발송 결과 응답 DTO
 * POST /internal/push/broadcast 처리 완료 후 반환되는 발송 결과 데이터입니다.
 *
 * @since   : 2026.04.11
 * @version : 1.0.0
 * @author  : 신태훈
 */
@Getter
@Builder
public class BroadcastResultResponse {

	/** 전체 발송 대상 수 (push_enabled=1 이고 fcm_token 존재하는 사용자) */
	private int totalCount;

	/** FCM 발송 성공 건수 */
	private int successCount;

	/** FCM 발송 실패 건수 */
	private int failCount;

	/** 결과 메시지 */
	private String message;

	/**
	 * 발송 결과를 기반으로 BroadcastResultResponse를 생성하는 정적 팩토리 메서드
	 *
	 * @param totalCount   : 전체 발송 시도 수
	 * @param successCount : 성공 건수
	 * @param failCount    : 실패 건수
	 * @return             : 브로드캐스트 결과 DTO
	 */
	public static BroadcastResultResponse of(int totalCount, int successCount, int failCount) {
		return BroadcastResultResponse.builder()
			.totalCount(totalCount)
			.successCount(successCount)
			.failCount(failCount)
			.message(String.format("브로드캐스트 완료: 전체 %d건, 성공 %d건, 실패 %d건", totalCount, successCount, failCount))
			.build();
	}
}
