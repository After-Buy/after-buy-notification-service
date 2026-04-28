package com.After_Buy.NotificationService.exception;

import com.After_Buy.NotificationService.client.AdminInternalClient;
import com.After_Buy.NotificationService.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * AOP 기반 스프링 최상위 통합 시스템 오류 관제 매니저
 * 컨트롤러와 서비스 전역에서 터져 나오는 예기치 못한 크러시 혹은 의도된 익셉션을 흡수하고 정제된 ApiResponse 포맷으로 치환해 방어합니다.
 *
 * @since : 2026.04.11
 * @version : 1.0.0
 * @author : 신태훈
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final AdminInternalClient adminInternalClient;


    /**
     * 커스텀 도메인 특화 비즈니스 예외 규격 핸들링 대응 함수
     * 우리가 정의한 CustomException 객체만을 캐치하여 정해진 HTTP 상태코드와 사전에 구상된 오류 메시지로 치환해줍니다.
     *
     * @param e : 서비스 로직 수행 중 방어 룰에 의해 고의로 팅겨져 던져진 익셉션 개체
     * @return : 일관된 공통 응답 래퍼 ApiResponse 안에 포장되어 있는 HTTP 리스폰스 엔티티 모델
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        log.warn("CustomException: status={}, code={}, message={}", e.getStatus(), e.getErrorCode(), e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
    }

    /**
     * 리퀘스트 바디 맵핑 및 밸리데이션 규정 침해 실패 핸들링 대응 함수
     * DTO에서 어노테이션 기반 파라미터 검증망에 문제가 포착되었을 시 검사 객체 내부 첫 번째 거부 코멘트를 오류 원인으로 매핑합니다.
     *
     * @param e : 클라이언트 폼 입력/매개변수 유효성 검사에서 불합격 판정 후 스프링이 터트린 자체 객체
     * @return : 어떤 파라미터 항목이 문제였는지 이유를 구체적으로 알려주는 친절한 BAD_REQUEST 상태 포맷
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("요청 파라미터가 올바르지 않습니다.");
        log.warn("Validation 실패: {}", message);
        return ResponseEntity.badRequest().body(ApiResponse.error("INVALID_REQUEST", message));
    }

    /**
     * 알 수 없는 최상단 런타임 익셉션 및 서버 크래시 백업용 언노운 예외 핸들링 함수
     * 시스템 예측망을 이탈한 치명적인 오류 발생 시 서버 내부 구조 노출 없이 500 응답으로 치환합니다.
     * Admin Service로 비동기 에러 로그를 전송합니다. (Fire & Forget)
     *
     * @param e       : 컨트롤러 필터 통제 밖까지 도달하고 삐져나온 자바/네트워크의 원시 레거시 익셉션
     * @param request : 현재 HTTP 요청 (path 추출용)
     * @return : 서버 크래시 500 응답
     * @since : 2026.04.26
     * @author : 신태훈
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e, HttpServletRequest request) {
        log.error("처리되지 않은 예외 발생: {}", e.getMessage(), e);

        /* Admin Service로 에러 로그 비동기 전송 (Fire & Forget, 4xx 제외 500급만) */
        adminInternalClient.sendErrorLogAsync(request.getRequestURI(), e);

        return ResponseEntity.internalServerError().body(ApiResponse.error("INTERNAL_ERROR", "서버 내부 오류가 발생했습니다."));
    }
}

