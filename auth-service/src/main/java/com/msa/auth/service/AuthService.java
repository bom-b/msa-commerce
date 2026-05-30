package com.msa.auth.service;

import com.msa.auth.config.JwtProperties;
import com.msa.auth.domain.User;
import com.msa.auth.domain.UserBalance;
import com.msa.auth.dto.BalanceResponse;
import com.msa.auth.dto.LoginRequest;
import com.msa.auth.dto.LoginResponse;
import com.msa.auth.repository.UserBalanceRepository;
import com.msa.auth.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.NoSuchElementException;

/**
 * 인증 비즈니스 로직을 담당하는 서비스. DB에서 사용자 자격증명을 검증하고 JWT를 발급한다.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    /** JWT 설정 프로퍼티. */
    private final JwtProperties jwtProperties;

    /** 사용자 데이터 접근 객체. */
    private final UserRepository userRepository;

    /** 사용자 예치금 데이터 접근 객체. */
    private final UserBalanceRepository userBalanceRepository;

    /**
     * 로그인 요청을 DB에서 검증하고 JWT를 발급한다.
     *
     * @param request 로그인 요청 (id, password)
     * @return 발급된 JWT를 담은 응답 DTO
     * @throws ResponseStatusException 사용자가 존재하지 않거나 비밀번호가 일치하지 않을 경우 {@code 401 UNAUTHORIZED}
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!user.getPassword().equals(request.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return new LoginResponse(generateToken(user.getId()));
    }

    /**
     * 사용자 ID로 예치금 정보를 조회한다.
     *
     * @param userId 조회할 사용자 ID
     * @return 사용자 ID와 잔액을 담은 응답 DTO
     * @throws NoSuchElementException 해당 사용자의 예치금 정보가 존재하지 않는 경우
     */
    @Transactional(readOnly = true)
    public BalanceResponse getBalance(Long userId) {
        UserBalance balance = userBalanceRepository.findByUser_Id(userId)
            .orElseThrow(() -> new NoSuchElementException("예치금 정보를 찾을 수 없습니다. userId=" + userId));
        return new BalanceResponse(userId, balance.getBalance());
    }

    /**
     * 사용자 예치금을 충전한다.
     *
     * @param userId 충전할 사용자 ID
     * @param amount 충전 금액 (양수)
     * @return 충전 후 잔액을 담은 응답 DTO
     * @throws IllegalArgumentException 충전 금액이 0 이하인 경우
     * @throws NoSuchElementException 예치금 정보가 존재하지 않는 경우
     */
    @Transactional
    public BalanceResponse charge(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }
        UserBalance balance = userBalanceRepository.findByUser_Id(userId)
            .orElseThrow(() -> new NoSuchElementException("예치금 정보를 찾을 수 없습니다. userId=" + userId));
        balance.charge(amount);
        return new BalanceResponse(userId, balance.getBalance());
    }

    /**
     * 사용자 예치금을 차감한다.
     *
     * @param userId 차감할 사용자 ID
     * @param amount 차감 금액 (양수)
     * @throws NoSuchElementException   예치금 정보가 존재하지 않는 경우
     * @throws IllegalArgumentException 잔액이 부족한 경우
     */
    @Transactional
    public void deduct(Long userId, BigDecimal amount) {
        UserBalance balance = userBalanceRepository.findByUser_Id(userId)
            .orElseThrow(() -> new NoSuchElementException("예치금 정보를 찾을 수 없습니다. userId=" + userId));
        balance.deduct(amount);
    }

    /**
     * 사용자 ID를 subject로 하는 서명된 JWT를 생성한다.
     *
     * @param userIdx JWT의 subject로 설정할 사용자 ID
     * @return HS256으로 서명된 JWT 문자열
     */
    private String generateToken(Long userIdx) {
        SecretKey key = Keys.hmacShaKeyFor(
            jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        return Jwts.builder()
            .subject(userIdx.toString())
            .issuedAt(now)
            .expiration(new Date(now.getTime() + jwtProperties.getExpirationMs()))
            .signWith(key)
            .compact();
    }
}
