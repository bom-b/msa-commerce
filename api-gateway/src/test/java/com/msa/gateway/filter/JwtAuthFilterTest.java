package com.msa.gateway.filter;

import com.msa.gateway.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link JwtAuthFilter} 단위 테스트.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    /**
     * 테스트용 256비트 이상의 HMAC 비밀키.
     */
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
    @DisplayName("화이트리스트 경로는 JWT 검증 없이 다음 필터로 전달")
    void filter_whitelistedPath_skipsAuthentication() {
        MockServerWebExchange exchange = exchangeFor(
            MockServerHttpRequest.post("/auth/login").build());
        GatewayFilterChain chain = chainReturningEmpty();

        filter.filter(exchange, chain).block();

        verify(chain).filter(any());
    }

    /**
     * 유효한 JWT가 포함된 요청은 다음 필터로 전달되며,
     * 다운스트림 요청의 {@code X-User-Id} 헤더에 JWT subject 값이 설정되어야 한다.
     */
    @Test
    @DisplayName("유효한 토큰 요청 시 X-User-Id 헤더에 subject 설정 후 전달")
    void filter_withValidToken_forwardsRequestWithUserId() {
        String token = createToken("test");
        MockServerWebExchange exchange = exchangeFor(
            MockServerHttpRequest.get("/orders/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build());
        GatewayFilterChain chain = chainReturningEmpty();
        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);

        filter.filter(exchange, chain).block();

        verify(chain).filter(captor.capture());
        String userId = captor.getValue().getRequest().getHeaders().getFirst("X-User-Id");
        assertThat(userId).isEqualTo("test");
    }

    /**
     * 클라이언트가 {@code X-User-Id} 헤더를 직접 위조하여 전송한 경우,
     * 다운스트림 요청에서 해당 값이 JWT subject 값으로 교체되어야 한다.
     */
    @Test
    @DisplayName("위조된 X-User-Id 헤더는 JWT subject 값으로 교체")
    void filter_withForgedUserIdHeader_replacesWithJwtSubject() {
        String token = createToken("test");
        MockServerWebExchange exchange = exchangeFor(
            MockServerHttpRequest.get("/orders/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("X-User-Id", "attacker")  // 클라이언트가 위조한 헤더
                .build());
        GatewayFilterChain chain = chainReturningEmpty();
        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);

        filter.filter(exchange, chain).block();

        verify(chain).filter(captor.capture());
        String userId = captor.getValue().getRequest().getHeaders().getFirst("X-User-Id");
        assertThat(userId).isEqualTo("test");       // JWT subject 값으로 교체됨
        assertThat(userId).isNotEqualTo("attacker"); // 위조된 값이 그대로 전달되지 않음
    }

    /**
     * 전달되는 다운스트림 요청에 {@code X-Gateway-Request=true} 마커 헤더가 주입되어야 한다.
     */
    @Test
    @DisplayName("전달 요청에 X-Gateway-Request 마커 헤더 주입")
    void filter_forwardsRequestWithGatewayMarker() {
        MockServerWebExchange exchange = exchangeFor(
            MockServerHttpRequest.get("/orders/1").build());
        GatewayFilterChain chain = chainReturningEmpty();
        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);

        filter.filter(exchange, chain).block();

        verify(chain).filter(captor.capture());
        String marker = captor.getValue().getRequest().getHeaders().getFirst("X-Gateway-Request");
        assertThat(marker).isEqualTo("true");
    }

    /**
     * 클라이언트가 {@code X-Gateway-Request} 헤더에 위조값을 전송하더라도,
     * 다운스트림 요청에서 해당 값이 {@code "true"}로 덮어써져야 한다.
     */
    @Test
    @DisplayName("위조된 게이트웨이 마커는 true로 덮어쓰기")
    void filter_withForgedGatewayMarker_overwritesWithTrue() {
        MockServerWebExchange exchange = exchangeFor(
            MockServerHttpRequest.get("/orders/1")
                .header("X-Gateway-Request", "false")  // 클라이언트가 위조한 마커
                .build());
        GatewayFilterChain chain = chainReturningEmpty();
        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);

        filter.filter(exchange, chain).block();

        verify(chain).filter(captor.capture());
        String marker = captor.getValue().getRequest().getHeaders().getFirst("X-Gateway-Request");
        assertThat(marker).isEqualTo("true");  // 위조값이 "true"로 교체됨
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
