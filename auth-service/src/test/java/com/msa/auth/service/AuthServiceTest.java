package com.msa.auth.service;

import com.msa.auth.config.JwtProperties;
import com.msa.auth.dto.LoginRequest;
import com.msa.auth.dto.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link AuthService} 단위 테스트.
 *
 * <p>Spring 컨텍스트 없이 JWT 발급 및 인증 검증 로직만 격리하여 테스트한다.</p>
 */
class AuthServiceTest {

    /** 테스트용 256비트 이상의 HMAC 비밀키. */
    private static final String TEST_SECRET =
            "test-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256";

    private AuthService authService;

    /**
     * 각 테스트 실행 전 서비스 인스턴스를 초기화한다.
     */
    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret(TEST_SECRET);
        props.setExpirationMs(3600000L);
        authService = new AuthService(props);
    }

    /**
     * 올바른 자격증명({@code test/test})으로 로그인 시 비어있지 않은 JWT가 반환되어야 한다.
     */
    @Test
    void login_withValidCredentials_returnsNonBlankToken() {
        LoginRequest request = loginRequest("test", "test");

        LoginResponse response = authService.login(request);

        assertThat(response.token()).isNotBlank();
    }

    /**
     * 존재하지 않는 ID로 로그인 시 {@code 401 UNAUTHORIZED} 예외가 발생해야 한다.
     */
    @Test
    void login_withInvalidId_throwsUnauthorized() {
        LoginRequest request = loginRequest("wrong", "test");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex ->
                    assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    /**
     * 올바르지 않은 비밀번호로 로그인 시 {@code 401 UNAUTHORIZED} 예외가 발생해야 한다.
     */
    @Test
    void login_withInvalidPassword_throwsUnauthorized() {
        LoginRequest request = loginRequest("test", "wrong");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex ->
                    assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    /**
     * 테스트용 {@link LoginRequest} 객체를 생성하는 헬퍼 메서드.
     *
     * @param id       사용자 ID
     * @param password 비밀번호
     * @return 생성된 {@link LoginRequest}
     */
    private LoginRequest loginRequest(String id, String password) {
        return new LoginRequest(id, password);
    }
}
