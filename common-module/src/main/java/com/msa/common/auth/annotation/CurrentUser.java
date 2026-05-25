package com.msa.common.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 메서드 파라미터에 현재 인증된 사용자 ID를 주입하는 어노테이션.
 *
 * <p>{@link com.msa.common.auth.resolver.CurrentUserArgumentResolver}가 이 어노테이션이 붙은
 * {@code Long} 타입 파라미터에 {@code X-User-Id} 헤더 값을 파싱하여 주입한다.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}
