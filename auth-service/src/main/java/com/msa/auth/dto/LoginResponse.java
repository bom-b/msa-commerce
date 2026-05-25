package com.msa.auth.dto;

/**
 * 로그인 응답 DTO. 로그인 성공 시 클라이언트에 반환되는 JWT 토큰을 담는 데이터 객체.
 *
 * @param token 발급된 JWT 액세스 토큰
 */
public record LoginResponse(String token) {
}
