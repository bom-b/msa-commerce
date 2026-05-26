package com.msa.auth.controller;

import com.msa.auth.dto.BalanceResponse;
import com.msa.auth.dto.LoginResponse;
import com.msa.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link AuthController} HTTP 계층 슬라이스 테스트.
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    /** MockMvc HTTP 요청 수행 객체. */
    @Autowired
    private MockMvc mockMvc;

    /** AuthService Mock 객체. */
    @MockBean
    private AuthService authService;

    /**
     * 올바른 자격증명으로 로그인 시 {@code 200 OK}와 JWT 토큰이 반환되어야 한다.
     */
    @Test
    void login_withValidCredentials_returns200WithToken() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponse("mocked-jwt-token"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"test\",\"password\":\"test\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }

    /**
     * 올바르지 않은 자격증명으로 로그인 시 {@code 401 UNAUTHORIZED}가 반환되어야 한다.
     */
    @Test
    void login_withInvalidCredentials_returns401() throws Exception {
        when(authService.login(any()))
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"wrong\",\"password\":\"wrong\"}"))
            .andExpect(status().isUnauthorized());
    }

    /**
     * 유효한 X-User-Id 헤더로 예치금 조회 시 {@code 200 OK}와 잔액이 반환되어야 한다.
     */
    @Test
    void getBalance_withValidUserId_returns200WithBalance() throws Exception {
        when(authService.getBalance(eq(1L))).thenReturn(new BalanceResponse(1L, BigDecimal.valueOf(50000)));

        mockMvc.perform(get("/auth/balance")
                .header("X-User-Id", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.balance").value(50000));
    }

    /**
     * 존재하지 않는 사용자 ID로 예치금 조회 시 {@code 404 NOT FOUND}가 반환되어야 한다.
     */
    @Test
    void getBalance_withUnknownUserId_returns404() throws Exception {
        when(authService.getBalance(eq(999L))).thenThrow(new NoSuchElementException("예치금 정보를 찾을 수 없습니다."));

        mockMvc.perform(get("/auth/balance")
                .header("X-User-Id", "999"))
            .andExpect(status().isNotFound());
    }

    /**
     * 유효한 금액으로 충전 요청 시 {@code 200 OK}와 충전 후 잔액이 반환되어야 한다.
     */
    @Test
    void charge_withValidAmount_returns200WithUpdatedBalance() throws Exception {
        when(authService.charge(eq(1L), any())).thenReturn(new BalanceResponse(1L, BigDecimal.valueOf(60000)));

        mockMvc.perform(post("/auth/balance/charge")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":10000}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value(60000));
    }

    /**
     * 0 금액으로 충전 요청 시 {@code 400 Bad Request}가 반환되어야 한다.
     */
    @Test
    void charge_withZeroAmount_returns400() throws Exception {
        when(authService.charge(eq(1L), argThat(a -> a != null && a.compareTo(BigDecimal.ZERO) == 0))).thenThrow(new IllegalArgumentException("충전 금액은 0보다 커야 합니다."));

        mockMvc.perform(post("/auth/balance/charge")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":0}"))
            .andExpect(status().isBadRequest());
    }
}
