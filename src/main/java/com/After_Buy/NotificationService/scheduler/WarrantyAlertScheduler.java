package com.After_Buy.NotificationService.scheduler;

import com.After_Buy.NotificationService.client.InternalDeviceClient;
import com.After_Buy.NotificationService.entity.Notification;
import com.After_Buy.NotificationService.entity.PushSettings;
import com.After_Buy.NotificationService.repository.NotificationRepository;
import com.After_Buy.NotificationService.repository.PushSettingsRepository;
import com.After_Buy.NotificationService.service.FcmPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 보증 만료 알림 스케줄러
 * 매일 오전 09:00에 Device Service로부터 보증 만료 임박 기기 목록을 수신하여
 * FCM 푸시 알림 발송 및 notifications 테이블에 이력을 저장합니다.
 * 동일 (device_id, notification_type, 날짜) 조합의 중복 발송을 방지합니다.
 *
 * @since   : 2026.04.11
 * @version : 1.0.0
 * @author  : 신태훈
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WarrantyAlertScheduler {

	private final InternalDeviceClient internalDeviceClient;
	private final NotificationRepository notificationRepository;
	private final PushSettingsRepository pushSettingsRepository;
	private final FcmPushService fcmPushService;

	/**
	 * 보증 만료 알림 발송 크론잡 — 매일 오전 09:00 실행
	 * days=[30, 14, 1, 0] 순서로 순회하며 각 만료 임박 타입별 FCM 발송을 처리합니다.
	 */
	@Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
	public void sendWarrantyAlerts() {
		log.info("=== 보증 만료 알림 스케줄러 시작 ===");

		int[][] dayTypeMap = {
			{30, Notification.NotificationType.WARRANTY_D30.ordinal()},
			{14, Notification.NotificationType.WARRANTY_D14.ordinal()},
			{1,  Notification.NotificationType.WARRANTY_D1.ordinal()},
			{0,  Notification.NotificationType.WARRANTY_EXPIRED.ordinal()}
		};

		Notification.NotificationType[] types = Notification.NotificationType.values();

		for (int[] entry : dayTypeMap) {
			int days = entry[0];
			Notification.NotificationType type = types[entry[1]];
			processAlert(days, type);
		}

		log.info("=== 보증 만료 알림 스케줄러 완료 ===");
	}

	/**
	 * 특정 만료 임박 일수에 대한 알림 처리 메서드
	 * Device Service로부터 기기 목록을 조회한 후 각 기기별 FCM 발송 및 이력 저장을 처리합니다.
	 *
	 * @param days : 만료 임박 기준 일수
	 * @param type : 알림 유형 (WARRANTY_D30 등)
	 */
	@Transactional
	protected void processAlert(int days, Notification.NotificationType type) {
		List<Map<String, Object>> devices = internalDeviceClient.getWarrantyExpiringDevices(days);
		if (devices.isEmpty()) {
			log.debug("보증 만료 임박 기기 없음: days={}", days);
			return;
		}

		log.info("알림 처리 시작: type={}, 기기 수={}", type, devices.size());
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime startOfDay = now.with(LocalTime.MIN);
		LocalDateTime endOfDay   = now.with(LocalTime.MAX);

		for (Map<String, Object> device : devices) {
			try {
				Long deviceId = toLong(device.get("deviceId"));
				Long userId   = toLong(device.get("userId"));

				// ─── 중복 발송 방지 ───────────────────────────────────
				if (notificationRepository.existsByDeviceIdAndTypeAndDate(deviceId, type, startOfDay, endOfDay)) {
					log.debug("중복 발송 건너뜀: deviceId={}, type={}", deviceId, type);
					continue;
				}

				// ─── 알림 이력 저장 ──────────────────────────────────
				String warrantyExpiryDateStr = (String) device.get("warrantyExpiryDate");
				LocalDate warrantyExpiryDate = LocalDate.parse(warrantyExpiryDateStr);
				LocalDateTime autoDeleteAt = warrantyExpiryDate.plusDays(7).atStartOfDay();

				Notification notification = Notification.builder()
					.userId(userId)
					.deviceId(deviceId)
					.deviceName((String) device.get("deviceName"))
					.deviceImageUrl((String) device.get("deviceImageUrl"))
					.notificationType(type)
					.warrantyExpiryDate(warrantyExpiryDate)
					.autoDeleteAt(autoDeleteAt)
					.build();
				notificationRepository.save(notification);

				// ─── FCM 발송 (push_enabled=1, fcm_token 존재 시) ────
				Optional<PushSettings> pushSettings = pushSettingsRepository.findEnabledByUserId(userId);
				if (pushSettings.isPresent()) {
					String title = buildTitle(type);
					String body  = buildBody(type, (String) device.get("deviceName"), days);
					boolean sent = fcmPushService.sendPush(pushSettings.get().getFcmToken(), title, body);
					if (!sent) {
						log.warn("FCM 발송 실패: userId={}, deviceId={}, type={}", userId, deviceId, type);
					}
				} else {
					log.debug("FCM 발송 스킵 (push 비활성 또는 토큰 없음): userId={}", userId);
				}

			} catch (Exception e) {
				log.error("기기별 알림 처리 중 오류 발생 (건너뜀): deviceId={}, error={}",
					device.get("deviceId"), e.getMessage(), e);
			}
		}
	}

	/**
	 * 알림 유형에 따른 FCM 제목 생성
	 *
	 * @param type : 알림 유형
	 * @return     : FCM 푸시 알림 제목 문자열
	 */
	private String buildTitle(Notification.NotificationType type) {
		return switch (type) {
			case WARRANTY_D30      -> "보증기간 만료 30일 전 알림";
			case WARRANTY_D14      -> "보증기간 만료 14일 전 알림";
			case WARRANTY_D1       -> "보증기간 만료 1일 전 알림";
			case WARRANTY_EXPIRED  -> "보증기간이 만료되었습니다";
		};
	}

	/**
	 * 알림 유형 및 기기명에 따른 FCM 본문 생성
	 *
	 * @param type       : 알림 유형
	 * @param deviceName : 기기명
	 * @param days       : 만료까지 남은 일수
	 * @return           : FCM 푸시 알림 본문 문자열
	 */
	private String buildBody(Notification.NotificationType type, String deviceName, int days) {
		if (type == Notification.NotificationType.WARRANTY_EXPIRED) {
			return String.format("'%s'의 보증기간이 오늘로 만료되었습니다.", deviceName);
		}
		return String.format("'%s'의 보증기간이 %d일 후 만료됩니다. 지금 확인하세요.", deviceName, days);
	}

	/**
	 * Object → Long 안전 변환 유틸
	 *
	 * @param value : Map에서 추출한 Object 값
	 * @return      : Long 변환 결과
	 */
	private Long toLong(Object value) {
		if (value instanceof Number n) return n.longValue();
		return Long.parseLong(String.valueOf(value));
	}
}
