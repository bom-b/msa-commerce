package com.msa.common.exception;

/**
 * 전역 에러 응답 DTO.
 *
 * @param status  HTTP 상태 코드
 * @param message 클라이언트에 표시할 에러 메시지
 */
public record ErrorResponse(int status, String message) {

    /**
     * 상태 코드와 메시지로 ErrorResponse 를 생성한다.
     *
     * @param status  HTTP 상태 코드
     * @param message 에러 메시지
     * @return ErrorResponse 인스턴스
     */
    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(status, message);
    }
}
