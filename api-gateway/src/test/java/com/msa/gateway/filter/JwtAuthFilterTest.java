package com.msa.gateway.filter;

import com.msa.gateway.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link JwtAuthFilter} 단위 테스트.
 *
 * <p>{@link MockServerWebExchange}를 사용해 WebFlux 환경에서의
 * JWT 검증 필터 동작을 Spring 컨텍스트 없이 격리하여 테스트한다.</p>
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    /** 테스트용 256비트 이상의 HMAC 비밀키. */
    private static final String TEST_SECRET =
            "test-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256";

    private JwtAuthFilter filter;

    /**
     * 각 테스트 실행 전 필터 인스턴스를 초기화한다.
     */
    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret(TEST_SECRET);
        filter = new JwtAuthFilter(props);
    }

    /**
     * 화이트리스트 경로({@code /auth/login})에 대한 요청은 JWT 검증 없이 다음 필터로 전달되어야 한다.
     */
    @Test
    void filter_whitelistedPath_skipsAuthentication() {
        MockServerWebExchange exchange = exchangeFor(
                MockServerHttpRequest.post("/auth/login").build());
        GatewayFilterChain chain = chainReturningEmpty();

        filter.filter(exchange, chain).block();

        verify(chain).filter(any());
    }

    /**
     * 유효한 JWT가 포함된 요청은 다음 필터로 전달되어야 한다.
     */
    @Test
    void filter_withValidToken_forwardsRequestWithUserId() {
        String token = createToken("test");
        MockServerWebExchange exchange = exchangeFor(
                MockServerHttpRequest.get("/orders/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build());
        GatewayFilterChain chain = chainReturningEmpty();

        filter.filter(exchange, chain).block();

        verify(chain).filter(any());
    }

    /**
     * Authorization 헤더가 없는 요청에 대해 {@code 401 UNAUTHORIZED}를 반환해야 한다.
     */
    @Test
    void filter_withMissingAuthHeader_returnsUnauthorized() {
        MockServerWebExchange exchange = exchangeFor(
                MockServerHttpRequest.get("/orders/1").build());
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    /**
     * 서명이 올바르지 않은 JWT를 포함한 요청에 대해 {@code 401 UNAUTHORIZED}를 반환해야 한다.
     */
    @Test
    void filter_withInvalidToken_returnsUnauthorized() {
        MockServerWebExchange exchange = exchangeFor(
                MockServerHttpRequest.get("/orders/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                        .build());
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    /**
     * {@code Bearer } 접두사 없이 토큰만 전달된 요청에 대해 {@code 401 UNAUTHORIZED}를 반환해야 한다.
     */
    @Test
    void filter_withMalformedAuthHeader_returnsUnauthorized() {
        MockServerWebExchange exchange = exchangeFor(
                MockServerHttpRequest.get("/orders/1")
                        .header(HttpHeaders.AUTHORIZATION, "token-without-bearer-prefix")
                        .build());
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    /**
     * 테스트용 JWT 토큰을 생성한다.
     *
     * @param userId 토큰의 subject로 설정할 사용자 ID
     * @return 서명된 JWT 문자열
     */
    private String createToken(String userId) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key)
                .compact();
    }

    /**
     * {@link MockServerHttpRequest}로부터 {@link MockServerWebExchange}를 생성한다.
     *
     * @param request 목 HTTP 요청
     * @return 생성된 {@link MockServerWebExchange}
     */
    private MockServerWebExchange exchangeFor(MockServerHttpRequest request) {
        return MockServerWebExchange.from(request);
    }

    /**
     * {@link Mono#empty()}를 반환하는 목 필터 체인을 생성한다.
     *
     * @return 정상 처리를 시뮬레이션하는 목 {@link GatewayFilterChain}
     */
    private GatewayFilterChain chainReturningEmpty() {
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
        return chain;
    }
}
