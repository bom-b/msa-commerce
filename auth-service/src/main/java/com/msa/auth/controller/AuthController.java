package com.msa.auth.controller;

import com.msa.auth.dto.LoginRequest;
import com.msa.auth.dto.LoginResponse;
import com.msa.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 관련 HTTP 요청을 처리하는 컨트롤러.
 *
 * <p>로그인 요청을 받아 JWT를 발급하는 단일 엔드포인트를 제공한다.
 * API Gateway에서 이 경로({@code /auth/**})는 JWT 검증 없이 통과된다.</p>
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인 요청을 처리하고 JWT를 반환한다.
     *
     * @param request 로그인 요청 바디 (id, password)
     * @return {@code 200 OK}와 함께 발급된 JWT 토큰 반환.
     *         자격증명이 올바르지 않으면 {@code 401 UNAUTHORIZED} 반환.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
