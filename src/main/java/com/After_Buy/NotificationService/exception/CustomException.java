package com.After_Buy.NotificationService.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 마이크로 서비스 도메인 정책 전용 예외 부모 클래스
 * 비즈니스 로직 처리 과정 시 일어나는 다양한 결함과 요구 통제 문제사항들을 규격화된 상태 코드로 만들어 일원화하는 사용자 정의 예외 묶음 체계입니다.
 *
 * @since : 2026.04.11
 * @version : 1.0.0
 * @author : 신태훈
 */
@Getter
public class CustomException extends RuntimeException {

    // HTTP 통신 규약 상 브라우저가 분석 해석할 HTTP 기본 상태 표준 코드
    private final HttpStatus status;

    // 프론트엔드 비즈니스 통계나 클라이언트 분기 제어를 용이하게 하기 위한 고유 텍스트형 메시지 코드
    private final String errorCode;

    public CustomException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    /**
     * 비인가되거나 만료되어 접근 거절 시 조기 발생시키는 시스템 예외 발행기 팩토리 메소드
     *
     * @param message : JWT 토큰이 변형/거절되었거나 권한이 빈약한지에 대한 클라이언트 사유 코멘트
     * @return : 401 UNAUTHORIZED 사양의 커스텀 익셉션 포맷팅 객체 반환
     */
    public static CustomException unauthorized(String message) {
        return new CustomException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", message);
    }

    /**
     * 클라이언트 측 잘못된 조작, 범위 이탈, 규정 파라미터 불량 등의 공격 시 발구되는 예외 발행기
     *
     * @param code    : 정책 위반 내역을 축약하는 서비스 커스텀 오류 식별 텍스트
     * @param message : 무슨 입력이 불량이라 서버로부터 반려되었는지에 대한 상세 제재 이유
     * @return : 400 BAD REQUEST 사양의 오염 방지용 생성 예외 반환
     */
    public static CustomException badRequest(String code, String message) {
        return new CustomException(HttpStatus.BAD_REQUEST, code, message);
    }

    /**
     * 자원 찾기 실패, 레코드 누락 등에 사용하는 낫파운드 널 회피성 예외 발행기
     *
     * @param message : 존재하지 않는 리소스를 호출할 때의 알럿 문구
     * @return : 404 NOT FOUND 기반의 데이터 부재 탐지 전용 예외 덩어리 반환
     */
    public static CustomException notFound(String message) {
        return new CustomException(HttpStatus.NOT_FOUND, "NOT_FOUND", message);
    }

    /**
     * 타인의 자원에 접근하거나 권한 없는 조작 시도 시 발생시키는 예외 발행기
     *
     * @param message : 접근 거부 사유 메시지
     * @return : 403 FORBIDDEN 기반의 접근 금지 예외 반환
     */
    public static CustomException forbidden(String message) {
        return new CustomException(HttpStatus.FORBIDDEN, "FORBIDDEN", message);
    }

    /**
     * 서버 망 분리, 외부 의존성 이슈 등 개발자나 머신 자체의 셧다운 대응 발생 예외 생성기
     *
     * @param message : 어느 서비스와 통신이 실패하여 최상위 500 장애가 감지되었는지에 대한 보고 로그 메시지
     * @return : 500 INTERNAL SERVER ERROR 규약에 따른 시스템 치명 붕괴 시 예외 알림 반환
     */
    public static CustomException internalError(String message) {
        return new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", message);
    }
}
