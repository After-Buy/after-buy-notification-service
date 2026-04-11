package com.After_Buy.NotificationService.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 공통 API 응답 DTO
 * 앱 클라이언트 및 MSA 서비스 간 통일된 규격으로 응답을 내려주기 위한 공통 래퍼 클래스
 *
 * @since : 2026.04.11
 * @version : 1.0.0
 * @author : 신태훈
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    // 요청 성공 여부
    private final boolean success;

    // 응답 데이터 제네릭
    private final T data;

    // 성공 혹은 추가 메시지
    private final String message;

    // 실패 시 에러 상세 정보
    private final ErrorDetail error;

    private ApiResponse(boolean success, T data, String message, ErrorDetail error) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.error = error;
    }

    /**
     * 성공 응답 생성 (데이터만 존재)
     *
     * @param data : 응답 데이터
     * @return : 성공 상태의 ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    /**
     * 성공 응답 생성 (데이터 및 메시지 존재)
     *
     * @param data    : 응답 데이터
     * @param message : 전달할 메시지
     * @return : 성공 상태의 ApiResponse
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }

    /**
     * 성공 응답 생성 (메시지만 존재)
     *
     * @param message : 성공 알림 메시지
     * @return : 성공 상태의 데이터 없는 ApiResponse
     */
    public static ApiResponse<Void> successMessage(String message) {
        return new ApiResponse<>(true, null, message, null);
    }

    /**
     * 에러 응답 생성
     *
     * @param code    : 정의된 에러 코드
     * @param message : 에러 상세 설명
     * @return : 실패 상태의 ErrorDetail이 포함된 ApiResponse
     */
    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(false, null, null, new ErrorDetail(code, message));
    }

    /**
     * 상세 에러 정보 이너 클래스
     */
    @Getter
    @AllArgsConstructor
    public static class ErrorDetail {
        // 에러 코드
        private String code;
        // 에러 상세 메시지
        private String message;
    }
}
