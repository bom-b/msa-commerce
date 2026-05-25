package com.msa.auth.controller;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link AuthController} HTTP 계층 슬라이스 테스트.
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
}
