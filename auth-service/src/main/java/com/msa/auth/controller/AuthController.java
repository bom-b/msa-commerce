package com.msa.auth.controller;

import com.msa.auth.dto.BalanceResponse;
import com.msa.auth.dto.LoginRequest;
import com.msa.auth.dto.LoginResponse;
import com.msa.auth.service.AuthService;
import com.msa.common.auth.annotation.Authenticated;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 관련 HTTP 요청을 처리하는 컨트롤러. 로그인 및 예치금 조회 엔드포인트를 제공한다.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    /** 인증 비즈니스 로직 서비스. */
    private final AuthService authService;

    /**
     * 로그인 요청을 처리하고 JWT를 반환한다.
     *
     * @param request 로그인 요청 바디 (id, password)
     * @return {@code 200 OK}와 함께 발급된 JWT 토큰
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * 인증된 사용자의 예치금 잔액을 조회한다.
     *
     * @param userId X-User-Id 헤더에서 추출한 사용자 ID
     * @return {@code 200 OK}와 함께 사용자 예치금 정보
     */
    @Authenticated
    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(authService.getBalance(userId));
    }
}
