package com.After_Buy.NotificationService.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase Admin SDK 초기화 설정 클래스
 * FCM(Firebase Cloud Messaging) 푸시 알림 발송을 위한 Firebase 앱 인스턴스를 서버 구동 시 단 한 번 초기화합니다.
 * firebase.config.path 환경변수로 서비스 계정 JSON 파일 경로를 주입받습니다.
 *
 * @since : 2026.04.11
 * @version : 1.0.0
 * @author : 신태훈
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.config.path}")
    private String firebaseConfigPath;

    private final ResourceLoader resourceLoader;

    public FirebaseConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Firebase 앱 초기화 빈 등록 메서드
     * 서버 기동 시 서비스 계정 JSON을 읽어 FirebaseApp 인스턴스를 등록합니다.
     * 이미 초기화된 경우(재기동 등) 중복 등록을 방지하고 기존 인스턴스를 재사용합니다.
     *
     * @throws IOException : 서비스 계정 JSON 파일 읽기 실패 시 발생
     */
    @PostConstruct
    public void initializeFirebase() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("Firebase 앱이 이미 초기화되어 있습니다. 기존 인스턴스를 재사용합니다.");
            return;
        }
        Resource resource = resourceLoader.getResource(firebaseConfigPath);
        try (InputStream serviceAccount = resource.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK 초기화 완료: path={}", firebaseConfigPath);
        } catch (IOException e) {
            log.error("Firebase 서비스 계정 JSON 파일을 읽을 수 없습니다: path={}, error={}", firebaseConfigPath, e.getMessage());
            throw e;
        }
    }
}
