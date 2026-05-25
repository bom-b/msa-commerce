package com.msa.common.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 인증이 필요한 엔드포인트에 선언하는 어노테이션.
 *
 * <p>메서드 또는 클래스에 적용 가능하다. {@link com.msa.common.auth.interceptor.AuthInterceptor}가
 * 이 어노테이션을 감지하여 {@code X-User-Id} 헤더 존재 여부를 검사한다.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Authenticated {
}
