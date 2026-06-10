# After-Buy Notification Service

전자기기 보증기간 관리 앱 **After-Buy**의 알림 도메인을 담당하는 Spring Boot 기반 마이크로서비스입니다.  
보증기간 만료 알림 이력, FCM 푸시 발송, 푸시 설정, 관리자 공지 브로드캐스트, Auth / Device / Admin Service와의 내부 연동을 담당합니다.

## 담당 범위

- 사용자별 보증기간 알림 목록 조회, 읽음 처리, 수동 삭제
- 사용자별 푸시 설정 조회 및 FCM 토큰 갱신
- Auth Service의 신규 가입 / 푸시 ON-OFF / 회원 탈퇴 이벤트 처리
- Device Service에서 보증 만료 임박 기기 목록 조회
- 매일 스케줄러 기반 보증 만료 알림 생성 및 FCM 발송
- Admin Service의 공지성 전체 푸시 브로드캐스트 지원
- `X-Internal-Secret` 기반 내부 API 보호
- Lazy Initialization으로 비동기 초기화 실패 복구

## 기술 스택

| 구분 | 기술 |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3.5.12 |
| Security | Spring Security, JWT(JJWT), X-Internal-Secret |
| Database | MySQL, Spring Data JPA |
| Push | Firebase Admin SDK / FCM |
| Internal Communication | Spring WebFlux WebClient |
| Scheduler | Spring Scheduling |
| Docs | Springdoc OpenAPI / Swagger UI |
| Test | JUnit 5, Spring Boot Test |

## 핵심 구현

### 1. Lazy Initialization 기반 푸시 설정 복구

Auth Service는 신규 가입 시 Notification Service에 `push_settings` 생성을 비동기로 요청합니다.  
Notification Service가 일시적으로 내려가 있으면 이 초기화 요청이 실패할 수 있으므로, 다음 진입점에서 `push_settings`가 없으면 기본값으로 자동 생성합니다.

- 알림 목록 조회: `GET /api/notifications/home`
- 푸시 설정 조회: `GET /api/notifications/settings`
- FCM 토큰 갱신: `PATCH /api/notifications/settings`
- 푸시 ON/OFF 동기화 내부 API: `POST /internal/push-settings/sync`

기본값은 `push_enabled=1`, `fcm_token=null`입니다.  
이 구조 덕분에 비동기 초기화 실패가 사용자 가입 또는 앱 사용 실패로 이어지지 않습니다.

### 2. 보증 만료 알림 스케줄러

스케줄러가 Device Service의 내부 API를 호출해 만료 임박 기기를 조회하고, 알림 이력을 저장한 뒤 조건이 맞는 사용자에게 FCM을 발송합니다.

알림 기준:

- 보증 만료 30일 전: `WARRANTY_D30`
- 보증 만료 14일 전: `WARRANTY_D14`
- 보증 만료 1일 전: `WARRANTY_D1`
- 보증 만료 당일: `WARRANTY_EXPIRED`

중복 발송 방지를 위해 같은 날짜에 동일 `device_id + notification_type` 조합이 이미 존재하면 건너뜁니다.  
개별 기기의 알림 처리 중 오류가 발생해도 전체 스케줄러가 중단되지 않도록 예외를 격리했습니다.

### 3. FCM 발송과 브로드캐스트

Firebase Admin SDK를 사용해 단건 푸시와 관리자 공지 브로드캐스트를 처리합니다.

- 보증 만료 알림: 사용자별 FCM 토큰 대상 단건 발송
- 관리자 공지: `push_enabled=1`이고 `fcm_token`이 존재하는 전체 사용자 대상 발송
- 공지 푸시에는 `deep_link`, `announcement_id`를 data payload로 포함 가능
- 로그에는 FCM 토큰 전체를 남기지 않고 일부만 마스킹

### 4. 내부 API 보안

`/internal/**` 경로는 외부 사용자용 JWT 인증과 분리하고, `X-Internal-Secret` 헤더로 보호합니다.  
Auth Service와 Admin Service가 내부 이벤트를 전달할 때만 접근하도록 설계했습니다.

## 주요 API

### Notifications

| Method | Endpoint | 설명 |
| --- | --- | --- |
| GET | `/api/notifications/home` | 현재 사용자의 알림 목록 조회 |
| PATCH | `/api/notifications/{notificationId}/read` | 알림 읽음 처리 |
| DELETE | `/api/notifications/{notificationId}` | 알림 수동 삭제 |

### Push Settings

| Method | Endpoint | 설명 |
| --- | --- | --- |
| GET | `/api/notifications/settings` | 푸시 설정 조회 |
| PATCH | `/api/notifications/settings` | FCM 토큰 등록/갱신 |

### Internal

| Method | Endpoint | 설명 |
| --- | --- | --- |
| POST | `/internal/push-settings/init` | 신규 가입 사용자 푸시 설정 초기 생성 |
| POST | `/internal/push-settings/sync` | Auth Service의 푸시 ON/OFF 변경 동기화 |
| DELETE | `/internal/notifications/users/{userId}` | 탈퇴 사용자 알림/푸시 설정 전체 삭제 |
| POST | `/internal/push/broadcast` | 관리자 공지성 FCM 브로드캐스트 |

## 프로젝트 구조

```text
src/main/java/com/After_Buy/NotificationService/
├── client      # Device/Admin Service 내부 API WebClient
├── config      # Security, Firebase, Swagger, WebClient 설정
├── controller  # 사용자 알림 API, 푸시 설정 API, 내부 API
├── dto         # Request / Response DTO
├── entity      # Notification, PushSettings JPA Entity
├── exception   # CustomException, ErrorCode, GlobalExceptionHandler
├── repository  # Spring Data JPA Repository
├── scheduler   # 보증기간 만료 알림 스케줄러
├── security    # JWT Provider, Authentication Filter, UserPrincipal
└── service     # 알림 이력, 푸시 설정, FCM 발송 비즈니스 로직
```

## 실행 방법

### 1. 환경 변수 설정

프로젝트 루트에 `.env` 파일을 생성하거나 IDE 실행 환경변수로 주입합니다.

```properties
DB_HOST=localhost
DB_PORT=3306
DB_NAME=notification_db
DB_USERNAME=root
DB_PASSWORD=password

JWT_SECRET=...
JWT_ACCESS_EXPIRATION=3600000

INTERNAL_SECRET_KEY=...

FIREBASE_CONFIG_PATH=src/main/resources/firebase-service-account.json

SERVICES_DEVICE_URL=http://localhost:8082
SERVICES_ADMIN_URL=http://localhost:8084
```

`DB_NAME`에 지정한 MySQL 데이터베이스는 서버 실행 전에 미리 생성되어 있어야 합니다.

### 2. Firebase 서비스 계정 키 배치

FCM 발송을 위해 Firebase 서비스 계정 JSON 파일을 발급받고, `FIREBASE_CONFIG_PATH`에 해당 파일 경로를 지정합니다.  
서비스 계정 키는 민감 정보이므로 Git에 커밋하지 않습니다.

### 3. 빌드

```bash
./gradlew clean build
```

Windows 환경:

```bash
gradlew.bat clean build
```

### 4. 실행

```bash
./gradlew bootRun
```

기본 포트는 `8083`입니다.

### 5. Swagger

```text
http://localhost:8083/api/notifications/swagger-ui.html
```

## 설계 포인트

- Auth Service의 신규 가입 초기화 호출을 비동기로 받고, Notification Service 자체 Lazy Init으로 최종 일관성을 확보했습니다.
- 알림 ON/OFF 값은 Auth Service가 사용자 설정의 기준값을 갖고, Notification Service는 FCM 발송 판단에 필요한 복제 값을 유지합니다.
- 보증 알림 이력 저장과 FCM 발송을 분리해 FCM 실패가 알림 기록 자체를 막지 않도록 했습니다.
- 개별 기기 처리 실패가 전체 스케줄러 실패로 번지지 않도록 try-catch 범위를 기기 단위로 좁혔습니다.
- 관리자 공지 브로드캐스트는 Notification Service가 FCM 토큰과 수신 동의 상태를 가진 서비스라는 점을 활용해 내부 API로 제공했습니다.

