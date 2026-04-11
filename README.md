# After-Buy Notification Service (알림 서비스)

## 📌 프로젝트 소개

MSA 기반의 After-Buy 플랫폼 환경에서 사용자에게 보증 만료 임박 FCM 푸시 알림을 발송하고, 알림 내역 및 푸시 설정(수신 동의 등)을 전담하는 마이크로서비스입니다. Auth, Admin, Device 등 타 서비스와의 원활한 데이터 동기화 및 알림 전파 기능을 수행합니다.

## 🛠️ 기술 스택

- **Language**: Java 17
- **Framework**: Spring Boot 3.x, Spring Data JPA, Spring Security, Spring WebFlux
- **Database**: MySQL
- **Communication**: WebClient (Device Service 기기 정보 조회 등 내부 통신용)
- **Firebase**: Firebase Admin SDK (FCM 발송)
- **Security**: JWT (Json Web Token), X-Internal-Secret (내부 API)

## ✨ 주요 기능

- **자동 알림 스케줄러**: 매일 09시 보증 만료 임박 기기(D-30, 14, 1, 0)를 조회해 푸시 알림 발송 및 내역을 자동 기록합니다.
- **FCM 푸시 알림**: Firebase SDK를 통해 단건 푸시 및 전체 사용자를 대상으로 한 관리자 권한의 브로드캐스트 푸시를 지원합니다.
- **지연 초기화 (Lazy Initialization)**: 유연한 에러 복구와 불필요한 데이터 생성을 막기 위해, 푸시 설정이 없는 사용자가 목록이나 설정 조회 시점에 접근하면 기본값을 즉시 자동 생성합니다.
- **내부 통신 보안 (Zero Trust 설계)**: MSA 간 내부 통신을 보호하기 위한 전용 `X-Internal-Secret` 인증 헤더 필터를 운용합니다.
- **장애 격리 및 중복 방지**: 개별 기기에 대한 발송이 실패하더라도 스케줄러 전체 프로세스가 중단되지 않도록 예외를 격리하며, DB 조회를 통해 동일 날짜에 동일 유형의 중복 알림 생성을 차단합니다.

## 📁 폴더 구조

```text
src/main/java/com/After_Buy/NotificationService/
├── client      # Device Service 등 연동 타 MSA용 WebClient 클라이언트
├── config      # Security, Firebase SDK, Swagger, WebClient 인프라 설정
├── controller  # 앱 연동 사용자 알림 REST API 및 MSA 내부망 연동 API 엔드포인트
├── dto         # 계층 간 데이터 교환(Validation 적용) Request / Response 모델
├── entity      # Notification, PushSettings 등 데이터베이스 영속성(JPA) 객체
├── exception   # 서비스 전역 에러 제어용 핸들러 및 Custom Exception 커스텀 응답
├── repository  # 데이터베이스 접근을 담당하는 Spring Data JPA 구현 계층
├── scheduler   # 보증 알림 등 크론잡(Batch) 처리를 위한 프로세스
├── security    # JWT 토큰 검증, 파싱 등 권한 필터 인프라
└── service     # 푸시 발송 코어 관련 비즈니스 로직 (FCM, 알림 기록, 설정 연동)
```

## 🚀 Getting Started (서버 실행 방법)

### 1. 환경 변수 세팅

앱 구동 전 필수 환경변수 리스트입니다. 프로젝트 루트 경로에 `.env` 파일을 직접 생성한 뒤 다음 변수들을 기입하여 사용하거나 IDE 환경변수로 주입해 주세요.

- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` (MySQL 연동)
  > ⚠️ **데이터베이스 필수 조건**: 서버 구동 전 `DB_NAME`으로 지정한 이름(예: `notification_db`)의 데이터베이스가 존재해야 합니다. 지정된 테이블은 구동 시 자동 구축됩니다.
- `JWT_SECRET` (Auth Service와 반드시 동일해야 앱에서 넘어온 토큰을 파싱할 수 있습니다.)
- `INTERNAL_SECRET_KEY` (타 MSA와의 승인된 통신을 위한 내부 교환 키)
- `FIREBASE_CONFIG_PATH` (FCM 서비스 계정 파일 경로)
- `SERVICES_DEVICE_URL` (대상 Device MSA 주소)

### 2. Firebase 접근 키 배치

Firebase 클라우드 메시징 통신을 위해 인증을 거친 `firebase-service-account.json` 서비스 계정 키 파일을 발급받아 `src/main/resources/` 하위에 배치해야 합니다.

### 3. 프로젝트 빌드

터미널을 열고 프로젝트 루트 경로에서 아래 명렁어를 통해 테스트를 제외한 클린 빌드를 수행합니다.

```bash
# mac/linux의 경우: chmod +x gradlew
./gradlew clean build -x test
```

### 4. 로컬 서버 실행

기본적으로 8083번 포트를 사용하여 내장 톰캣 서버가 가동됩니다. `dev` 프로필을 활성화하여 시작하세요.

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 5. Swagger UI 활용 및 테스트

서버가 정상적으로 구동되었다면, 웹 브라우저에서 아래 주소로 접속해 API 명세를 확인할 수 있습니다.

```text
http://localhost:8083/api/notifications/swagger-ui/index.html
```
