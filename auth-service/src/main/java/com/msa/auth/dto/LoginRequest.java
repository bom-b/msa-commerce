package com.msa.auth.dto;

/**
 * 로그인 요청 DTO. {@code POST /auth/login} 요청 바디를 역직렬화하는 데이터 객체.
 *
 * @param username 사용자 ID
 * @param password 비밀번호
 */
public record LoginRequest(String username, String password) {
}
