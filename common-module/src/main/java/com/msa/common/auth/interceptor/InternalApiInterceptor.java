package com.msa.common.auth.interceptor;

import com.msa.common.auth.annotation.InternalApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * {@link InternalApi} 어노테이션이 선언된 핸들러에 대해 게이트웨이 경유 여부를 검사하는 인터셉터.
 *
 * <p>게이트웨이가 주입하는 {@code X-Gateway-Request} 헤더가 존재하면 외부 경유 요청으로 간주하여
 * {@code 403 Forbidden} JSON 응답을 반환하고 요청 처리를 중단한다.
 */
@Slf4j
public class InternalApiInterceptor implements HandlerInterceptor {

    /**
     * 게이트웨이가 전달하는 요청에 주입하는 마커 헤더명.
     */
    private static final String GATEWAY_HEADER = "X-Gateway-Request";

    /**
     * 핸들러 메서드 또는 클래스에 {@link InternalApi}가 선언된 경우 {@code X-Gateway-Request} 헤더 존재 여부를 검사한다.
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

        boolean internalOnly = handlerMethod.hasMethodAnnotation(InternalApi.class) || handlerMethod.getBeanType().isAnnotationPresent(InternalApi.class);

        if (!internalOnly) {
            return true;
        }

        if (request.getHeader(GATEWAY_HEADER) != null) {
            log.warn("내부 전용 엔드포인트에 게이트웨이 경유 요청 차단: {}", request.getRequestURI());
            writeForbidden(response, "Internal access only");
            return false;
        }

        return true;
    }

    /**
     * {@code 403 Forbidden} JSON 응답을 작성한다. {@link com.msa.common.exception.ErrorResponse} 형식과 동일한 구조를 사용한다.
     *
     * @param response HTTP 응답 객체
     * @param message  에러 메시지
     * @throws IOException 응답 쓰기 중 I/O 오류 발생 시
     */
    private void writeForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        byte[] body = ("{\"status\":403,\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        response.getOutputStream().write(body);
    }
}
