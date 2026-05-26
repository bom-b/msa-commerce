package com.msa.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

/**
 * 전역 예외 처리 핸들러. 모든 서비스에서 통일된 {@link ErrorResponse} 형식으로 에러를 반환한다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bean Validation 실패 시 첫 번째 필드 에러 메시지를 반환한다.
     *
     * @param e {@link MethodArgumentNotValidException} 유효성 검사 실패 예외
     * @return 400 Bad Request + 첫 번째 필드 에러 메시지
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(FieldError::getDefaultMessage)
            .orElse("잘못된 요청입니다.");
        log.warn("유효성 검사 실패: {}", message);
        return ResponseEntity.badRequest().body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), message));
    }

    /**
     * 요청한 리소스가 존재하지 않을 때 404를 반환한다.
     *
     * @param e {@link NoSuchElementException} 리소스 없음 예외
     * @return 404 Not Found + 예외 메시지
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElement(NoSuchElementException e) {
        log.warn("리소스를 찾을 수 없음: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(HttpStatus.NOT_FOUND.value(), e.getMessage()));
    }

    /**
     * {@link ResponseStatusException} 발생 시 예외 내 상태 코드와 메시지를 반환한다.
     *
     * @param e {@link ResponseStatusException} HTTP 상태 기반 예외
     * @return 예외에 지정된 HTTP 상태 코드 + reason 또는 메시지
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException e) {
        String message = (e.getReason() != null) ? e.getReason() : "요청을 처리할 수 없습니다.";
        log.warn("ResponseStatusException 발생: status={}, message={}", e.getStatusCode().value(), message);
        return ResponseEntity.status(e.getStatusCode()).body(ErrorResponse.of(e.getStatusCode().value(), message));
    }

    /**
     * 처리되지 않은 모든 예외에 대해 500을 반환한다. 내부 오류 메시지는 클라이언트에 노출하지 않는다.
     *
     * @param e {@link Exception} 예상치 못한 예외
     * @return 500 Internal Server Error + 고정 메시지
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("처리되지 않은 예외 발생: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 오류가 발생했습니다."));
    }
}
