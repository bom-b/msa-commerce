package com.msa.auth.service;

import com.msa.auth.config.JwtProperties;
import com.msa.auth.domain.User;
import com.msa.auth.dto.BalanceResponse;
import com.msa.auth.dto.LoginRequest;
import com.msa.auth.dto.LoginResponse;
import com.msa.auth.repository.UserBalanceRepository;
import com.msa.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.msa.auth.domain.UserBalance;

/**
 * {@link AuthService} 단위 테스트.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    /** 테스트용 256비트 이상의 HMAC 비밀키. */
    private static final String TEST_SECRET =
        "test-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256";

    /** Mock UserRepository. */
    @Mock
    private UserRepository userRepository;

    /** Mock UserBalanceRepository. */
    @Mock
    private UserBalanceRepository userBalanceRepository;

    /** 테스트 대상 서비스 인스턴스. */
    private AuthService authService;

    /**
     * 각 테스트 실행 전 서비스 인스턴스를 초기화한다.
     */
    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret(TEST_SECRET);
        props.setExpirationMs(3600000L);
        authService = new AuthService(props, userRepository, userBalanceRepository);
    }

    /**
     * 올바른 자격증명으로 로그인 시 비어있지 않은 JWT가 반환되어야 한다.
     */
    @Test
    void login_withValidCredentials_returnsNonBlankToken() {
        when(userRepository.findByUsername("test"))
            .thenReturn(Optional.of(new User(1L, "test", "test")));
        LoginRequest request = loginRequest("test", "test");

        LoginResponse response = authService.login(request);

        assertThat(response.token()).isNotBlank();
    }

    /**
     * 존재하지 않는 ID로 로그인 시 {@code 401 UNAUTHORIZED} 예외가 발생해야 한다.
     */
    @Test
    void login_withInvalidId_throwsUnauthorized() {
        when(userRepository.findByUsername("wrong")).thenReturn(Optional.empty());
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
        when(userRepository.findByUsername("test"))
            .thenReturn(Optional.of(new User(1L, "test", "test")));
        LoginRequest request = loginRequest("test", "wrong");

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(ex ->
                assertThat(((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    /**
     * 존재하는 사용자 ID로 예치금 조회 시 잔액이 반환되어야 한다.
     */
    @Test
    void getBalance_withValidUserId_returnsBalance() {
        User user = new User(1L, "test", "test");
        UserBalance userBalance = UserBalance.builder()
            .id(1L)
            .user(user)
            .balance(new BigDecimal("50000"))
            .build();
        when(userBalanceRepository.findByUser_Id(1L)).thenReturn(Optional.of(userBalance));

        BalanceResponse response = authService.getBalance(1L);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.balance()).isEqualByComparingTo(new BigDecimal("50000"));
    }

    /**
     * 예치금 정보가 없는 사용자 ID로 조회 시 {@link NoSuchElementException}이 발생해야 한다.
     */
    @Test
    void getBalance_withNotFoundUserId_throwsNoSuchElementException() {
        when(userBalanceRepository.findByUser_Id(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getBalance(99L))
            .isInstanceOf(NoSuchElementException.class);
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
