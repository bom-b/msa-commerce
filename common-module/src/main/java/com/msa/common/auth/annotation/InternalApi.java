package com.msa.common.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 내부 서비스 간 호출만 허용하는 엔드포인트에 선언하는 어노테이션.
 *
 * <p>메서드 또는 클래스에 적용 가능하다. 게이트웨이를 경유한(외부) 요청은
 * {@link com.msa.common.auth.interceptor.InternalApiInterceptor}가 403으로 차단한다.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface InternalApi {
}
