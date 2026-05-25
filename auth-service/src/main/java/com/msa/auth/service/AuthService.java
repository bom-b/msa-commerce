package com.msa.auth.service;

import com.msa.auth.config.JwtProperties;
import com.msa.auth.dto.LoginRequest;
import com.msa.auth.dto.LoginResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 인증 비즈니스 로직을 담당하는 서비스. 사용자 자격증명을 검증하고 JWT를 발급한다.
 * 학습 목적으로 {@code test/test} 계정만 허용한다.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProperties jwtProperties;

    /**
     * 로그인 요청을 검증하고 JWT를 발급한다.
     *
     * @param request 로그인 요청 (id, password)
     * @return 발급된 JWT를 담은 응답 DTO
     * @throws ResponseStatusException id 또는 password가 올바르지 않을 경우 {@code 401 UNAUTHORIZED}
     */
    public LoginResponse login(LoginRequest request) {
        if (!"test".equals(request.id()) || !"test".equals(request.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return new LoginResponse(generateToken(request.id()));
    }

    /**
     * 사용자 ID를 subject로 하는 서명된 JWT를 생성한다.
     *
     * @param userId JWT의 subject로 설정할 사용자 ID
     * @return HS256으로 서명된 JWT 문자열
     */
    private String generateToken(String userId) {
        SecretKey key = Keys.hmacShaKeyFor(
            jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        return Jwts.builder()
            .subject(userId)
            .issuedAt(now)
            .expiration(new Date(now.getTime() + jwtProperties.getExpirationMs()))
            .signWith(key)
            .compact();
    }
}
