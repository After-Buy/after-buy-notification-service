package com.After_Buy.NotificationService.repository;

import com.After_Buy.NotificationService.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Notification 엔티티 JPA 리포지토리
 * 알림 목록 조회(자동 삭제 필터링), 회원 탈퇴 시 일괄 삭제, 스케줄러 자동 삭제 등 쿼리를 담당합니다.
 *
 * @since   : 2026.04.11
 * @version : 1.0.0
 * @author  : 신태훈
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

	/**
	 * 사용자의 유효한 알림 목록 조회
	 * auto_delete_at이 현재 시각 이후이거나 NULL인 알림만 반환합니다. (자동 삭제 전 숨김 처리)
	 *
	 * @param userId : 조회 대상 사용자 ID
	 * @param now    : 현재 시각 (auto_delete_at 비교 기준)
	 * @return       : 유효한 알림 목록 (sentAt 내림차순)
	 */
	@Query("SELECT n FROM Notification n WHERE n.userId = :userId " +
		"AND (n.autoDeleteAt IS NULL OR n.autoDeleteAt > :now) " +
		"ORDER BY n.sentAt DESC")
	List<Notification> findActiveByUserId(@Param("userId") Long userId,
	                                      @Param("now") LocalDateTime now);

	/**
	 * 회원 탈퇴 시 사용자의 모든 알림 일괄 삭제
	 * Auth Service의 DELETE /internal/notifications/users/{userId} 수신 시 사용합니다.
	 *
	 * @param userId : 탈퇴 처리 대상 사용자 ID
	 */
	@Modifying
	@Query("DELETE FROM Notification n WHERE n.userId = :userId")
	void deleteAllByUserId(@Param("userId") Long userId);

	/**
	 * 스케줄러 자동 삭제 대상 알림 목록 조회
	 * auto_delete_at이 현재 시각 이전(과거)인 알림을 반환합니다.
	 *
	 * @param now : 현재 시각
	 * @return    : 삭제 대상 알림 목록
	 */
	@Query("SELECT n FROM Notification n WHERE n.autoDeleteAt IS NOT NULL AND n.autoDeleteAt <= :now")
	List<Notification> findExpiredNotifications(@Param("now") LocalDateTime now);

	/**
	 * 스케줄러 중복 발송 방지 조회
	 * 동일 (deviceId, notificationType, 날짜) 조합의 알림이 이미 존재하면 중복 발송을 건너뜁니다.
	 *
	 * @param deviceId           : 기기 ID
	 * @param notificationType   : 알림 유형 (WARRANTY_D30 등)
	 * @param startOfDay         : 해당 날짜 00:00:00
	 * @param endOfDay           : 해당 날짜 23:59:59
	 * @return                   : 중복 알림 존재 여부
	 */
	@Query("SELECT COUNT(n) > 0 FROM Notification n " +
		"WHERE n.deviceId = :deviceId " +
		"AND n.notificationType = :notificationType " +
		"AND n.sentAt >= :startOfDay AND n.sentAt <= :endOfDay")
	boolean existsByDeviceIdAndTypeAndDate(
		@Param("deviceId") Long deviceId,
		@Param("notificationType") Notification.NotificationType notificationType,
		@Param("startOfDay") LocalDateTime startOfDay,
		@Param("endOfDay") LocalDateTime endOfDay
	);
}
