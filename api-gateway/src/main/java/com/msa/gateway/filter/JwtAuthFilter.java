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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT 인증을 처리하는 Gateway 글로벌 필터.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    /**
     * 인증 검증을 건너뛸 경로 목록.
     */
    private static final List<String> AUTH_WHITELIST = List.of("/auth/");

    private final JwtProperties jwtProperties;

    /**
     * 요청의 JWT를 검증하고, 유효한 경우 {@code X-User-Id} 헤더를 신뢰 가능한 값으로 교체하여
     * 다운스트림으로 전달한다.
     *
     * @param exchange 현재 서버 요청/응답 교환 객체
     * @param chain    다음 필터로 요청을 전달하는 필터 체인
     * @return 처리 완료를 나타내는 {@link Mono}
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();

        // 화이트리스트 경로는 JWT 검증 없이 통과
        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange.getResponse(), "Missing or invalid Authorization header");
        }

        try {
            Claims claims = parseToken(authHeader.substring(7));

            // 클라이언트가 X-User-Id 헤더를 임의로 설정하는 것을 방지하기 위해
            // set()으로 기존 헤더를 완전히 제거한 뒤 JWT에서 추출한 신뢰 가능한 값으로 덮어씀
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .headers(h -> h.set("X-User-Id", claims.getSubject()))
                .build();
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } catch (Exception e) {
            log.warn("JWT 검증 실패: {}", e.getMessage());
            return unauthorized(exchange.getResponse(), "Invalid or expired token");
        }
    }

    /**
     * 요청 경로가 인증 화이트리스트에 포함되는지 확인한다.
     *
     * @param path 검사할 요청 경로
     * @return 화이트리스트에 포함되면 {@code true}
     */
    private boolean isWhitelisted(String path) {
        return AUTH_WHITELIST.stream().anyMatch(path::startsWith);
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
     * {@code 401 Unauthorized} 응답을 JSON 형식으로 작성한다.
     *
     * @param response 서버 HTTP 응답 객체
     * @param message  클라이언트에 전달할 에러 메시지
     * @return 응답 쓰기 완료를 나타내는 {@link Mono}
     */
    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = ("{\"error\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
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
