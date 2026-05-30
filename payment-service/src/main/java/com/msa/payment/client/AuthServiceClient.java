package com.msa.payment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.common.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

/**
 * Auth Service REST 클라이언트.
 */
@Slf4j
@Component
public class AuthServiceClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    /**
     * @param restClient   authRestClient 빈
     * @param objectMapper Jackson ObjectMapper
     */
    public AuthServiceClient(@Qualifier("authRestClient") RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 사용자 예치금을 차감한다.
     *
     * @param userId 차감할 사용자 ID
     * @param amount 차감 금액
     * @throws IllegalArgumentException 잔액 부족 또는 사용자 없음 (4xx 응답)
     */
    public void deduct(Long userId, BigDecimal amount) {
        try {
            restClient.post()
                .uri("/auth/balance/deduct")
                .body(new DeductRequest(userId, amount))
                .retrieve()
                .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            String reason = extractMessage(e);
            log.warn("예치금 차감 실패 - userId: {}, reason: {}", userId, reason);
            throw new IllegalArgumentException(reason);
        } catch (HttpServerErrorException e) {
            log.error("Auth Service 오류 - userId: {}, status: {}", userId, e.getStatusCode());
            throw e;
        }
    }

    /**
     * HTTP 에러 응답 본문에서 메시지를 추출한다.
     *
     * @param e HttpClientErrorException
     * @return 에러 메시지
     */
    private String extractMessage(HttpClientErrorException e) {
        try {
            ErrorResponse error = objectMapper.readValue(e.getResponseBodyAsString(), ErrorResponse.class);
            return error.message();
        } catch (Exception ex) {
            return e.getMessage();
        }
    }

    /**
     * 예치금 차감 요청 DTO (내부 전용).
     *
     * @param userId 사용자 ID
     * @param amount 차감 금액
     */
    private record DeductRequest(Long userId, BigDecimal amount) {}
}
