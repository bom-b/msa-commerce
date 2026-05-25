package com.msa.gateway.filter;

import com.msa.gateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT를 선택적으로 파싱하여 {@code X-User-Id} 헤더를 주입하는 Gateway 글로벌 필터.
 *
 * <p>JWT가 없거나 유효하지 않아도 요청을 차단하지 않는다. 인증 강제는 각 서비스의
 * {@code AuthInterceptor}가 담당한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    /** JWT 설정 프로퍼티. */
    private final JwtProperties jwtProperties;

    /**
     * 요청의 {@code X-User-Id} 헤더를 제거한 뒤, Authorization 헤더에 유효한 JWT가 있으면
     * 파싱하여 subject를 {@code X-User-Id}로 재설정하고 다운스트림으로 전달한다.
     *
     * @param exchange 현재 서버 요청/응답 교환 객체
     * @param chain    다음 필터로 요청을 전달하는 필터 체인
     * @return 처리 완료를 나타내는 {@link Mono}
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // 클라이언트가 X-User-Id를 위조하지 못하도록 항상 제거
        ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate()
            .headers(h -> h.remove("X-User-Id"));

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                Claims claims = parseToken(authHeader.substring(7));
                requestBuilder.headers(h -> h.set("X-User-Id", claims.getSubject()));
            } catch (Exception e) {
                log.warn("JWT 파싱 실패 (요청은 계속 처리): {}", e.getMessage());
            }
        }

        return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
    }

    /**
     * JWT 문자열을 파싱하여 클레임을 반환한다.
     *
     * @param token 파싱할 JWT 문자열 (Bearer 접두사 제거 후)
     * @return 파싱된 JWT 클레임 객체
     * @throws io.jsonwebtoken.JwtException 토큰이 유효하지 않거나 만료된 경우
     */
    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(
            jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * 이 필터의 실행 순서를 반환한다.
     *
     * @return -1 (Spring Cloud Gateway 기본 필터보다 먼저 실행)
     */
    @Override
    public int getOrder() {
        return -1;
    }
}
