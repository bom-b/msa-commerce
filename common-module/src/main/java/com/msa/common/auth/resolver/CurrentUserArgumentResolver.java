package com.msa.common.auth.resolver;

import com.msa.common.auth.annotation.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link CurrentUser} 어노테이션이 붙은 {@code Long} 타입 파라미터에 {@code X-User-Id} 헤더 값을 주입하는 ArgumentResolver.
 */
@Slf4j
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * API Gateway가 JWT에서 추출하여 전달하는 사용자 ID 헤더명.
     */
    private static final String USER_ID_HEADER = "X-User-Id";

    /**
     * 파라미터에 {@link CurrentUser}가 선언되어 있고 타입이 {@code Long}인 경우 지원한다.
     *
     * @param parameter 메서드 파라미터 정보
     * @return 지원 여부
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
            && Long.class.isAssignableFrom(parameter.getParameterType());
    }

    /**
     * {@code X-User-Id} 헤더 값을 {@code Long}으로 파싱하여 반환한다.
     *
     * @param parameter     메서드 파라미터 정보
     * @param mavContainer  ModelAndView 컨테이너 (미사용)
     * @param webRequest    현재 웹 요청
     * @param binderFactory 데이터 바인더 팩토리 (미사용)
     * @return 파싱된 사용자 ID ({@code Long}), 헤더가 없으면 {@code null}
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            return null;
        }

        String userIdHeader = request.getHeader(USER_ID_HEADER);
        if (userIdHeader == null || userIdHeader.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            log.warn("X-User-Id 헤더 파싱 실패: {}", userIdHeader);
            return null;
        }
    }
}
