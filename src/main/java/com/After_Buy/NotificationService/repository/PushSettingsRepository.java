package com.After_Buy.NotificationService.repository;

import com.After_Buy.NotificationService.entity.PushSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * PushSettings 엔티티 JPA 리포지토리
 * FCM 토큰 조회, Lazy Init, 회원 탈퇴 삭제, 브로드캐스트 대상 조회 등 쿼리를 담당합니다.
 *
 * @since   : 2026.04.11
 * @version : 1.0.0
 * @author  : 신태훈
 */
@Repository
public interface PushSettingsRepository extends JpaRepository<PushSettings, Long> {

	/**
	 * 사용자 ID로 푸시 설정 조회 (Lazy Init 패턴의 핵심 메서드)
	 * 결과가 없을 경우 PushSettingsService에서 기본값으로 자동 생성합니다.
	 *
	 * @param userId : 조회 대상 사용자 ID
	 * @return       : PushSettings Optional (없으면 empty)
	 */
	Optional<PushSettings> findByUserId(Long userId);

	/**
	 * 회원 탈퇴 시 사용자의 푸시 설정 삭제
	 * Auth Service의 DELETE /internal/notifications/users/{userId} 수신 시 사용합니다.
	 *
	 * @param userId : 탈퇴 처리 대상 사용자 ID
	 */
	@Modifying
	@Query("DELETE FROM PushSettings p WHERE p.userId = :userId")
	void deleteByUserId(@Param("userId") Long userId);

	/**
	 * 브로드캐스트 FCM 발송 대상 전체 조회
	 * push_enabled = 1 이고 fcm_token이 존재하는 사용자 설정 목록을 반환합니다.
	 * Admin Service의 POST /internal/push/broadcast 처리 시 사용합니다.
	 *
	 * @return : FCM 발송 가능한 push_settings 목록
	 */
	@Query("SELECT p FROM PushSettings p WHERE p.pushEnabled = 1 AND p.fcmToken IS NOT NULL")
	List<PushSettings> findAllEnabledWithToken();

	/**
	 * 특정 사용자의 FCM 발송 가능 여부 확인
	 * push_enabled = 1 이고 fcm_token이 존재하는 경우에만 스케줄러에서 FCM을 발송합니다.
	 *
	 * @param userId : 확인 대상 사용자 ID
	 * @return       : 발송 가능한 PushSettings Optional
	 */
	@Query("SELECT p FROM PushSettings p WHERE p.userId = :userId AND p.pushEnabled = 1 AND p.fcmToken IS NOT NULL")
	Optional<PushSettings> findEnabledByUserId(@Param("userId") Long userId);
}
