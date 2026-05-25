package com.msa.common.auth.interceptor;

import com.msa.common.auth.annotation.Authenticated;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * {@link Authenticated} 어노테이션이 선언된 핸들러에 대해 {@code X-User-Id} 헤더 존재 여부를 검사하는 인터셉터.
 *
 * <p>헤더가 없으면 {@code 401 Unauthorized} JSON 응답을 반환하고 요청 처리를 중단한다.
 */
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    /**
     * API Gateway가 JWT에서 추출하여 전달하는 사용자 ID 헤더명.
     */
    private static final String USER_ID_HEADER = "X-User-Id";

    /**
     * 핸들러 메서드 또는 클래스에 {@link Authenticated}가 선언된 경우 {@code X-User-Id} 헤더를 검사한다.
     *
     * @param request  현재 HTTP 요청
     * @param response 현재 HTTP 응답
     * @param handler  실행될 핸들러 (HandlerMethod 또는 기타)
     * @return {@code true}이면 요청 처리를 계속하고, {@code false}이면 중단한다
     * @throws IOException 응답 쓰기 중 I/O 오류 발생 시
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        boolean requiresAuth = handlerMethod.hasMethodAnnotation(Authenticated.class)
            || handlerMethod.getBeanType().isAnnotationPresent(Authenticated.class);

        if (!requiresAuth) {
            return true;
        }

        String userId = request.getHeader(USER_ID_HEADER);
        if (userId == null || userId.isBlank()) {
            log.warn("인증 필요 엔드포인트에 X-User-Id 헤더 없음: {}", request.getRequestURI());
            writeUnauthorized(response, "Authentication required");
            return false;
        }

        return true;
    }

    /**
     * {@code 401 Unauthorized} JSON 응답을 작성한다.
     *
     * @param response HTTP 응답 객체
     * @param message  에러 메시지
     * @throws IOException 응답 쓰기 중 I/O 오류 발생 시
     */
    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        byte[] body = ("{\"error\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        response.getOutputStream().write(body);
    }
}
