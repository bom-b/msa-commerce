package com.msa.common.auth.interceptor;

import com.msa.common.auth.annotation.InternalApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link InternalApiInterceptor} 단위 테스트.
 */
class InternalApiInterceptorTest {

    /**
     * 게이트웨이가 주입하는 마커 헤더명.
     */
    private static final String GATEWAY_HEADER = "X-Gateway-Request";

    private InternalApiInterceptor interceptor;

    /**
     * 각 테스트 실행 전 인터셉터 인스턴스를 초기화한다.
     */
    @BeforeEach
    void setUp() {
        interceptor = new InternalApiInterceptor();
    }

    /**
     * {@link InternalApi}가 선언된 핸들러에 게이트웨이 마커 헤더가 있으면 403으로 차단되어야 한다.
     *
     * @throws Exception 핸들러 메서드 조회 또는 응답 쓰기 중 오류 발생 시
     */
    @Test
    void preHandle_internalApiWithGatewayHeader_blocksWith403() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(GATEWAY_HEADER, "true");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handler = handlerFor("internalEndpoint");

        boolean result = interceptor.preHandle(request, response, handler);

        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
    }

    /**
     * {@link InternalApi}가 선언된 핸들러에 게이트웨이 마커 헤더가 없으면 통과되어야 한다.
     *
     * @throws Exception 핸들러 메서드 조회 또는 응답 쓰기 중 오류 발생 시
     */
    @Test
    void preHandle_internalApiWithoutGatewayHeader_passes() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handler = handlerFor("internalEndpoint");

        boolean result = interceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    /**
     * {@link InternalApi}가 없는 핸들러는 게이트웨이 마커 헤더 유무와 무관하게 통과되어야 한다.
     *
     * @throws Exception 핸들러 메서드 조회 또는 응답 쓰기 중 오류 발생 시
     */
    @Test
    void preHandle_nonInternalApiHandler_passes() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(GATEWAY_HEADER, "true");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handler = handlerFor("publicEndpoint");

        boolean result = interceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
    }

    /**
     * handler가 {@link HandlerMethod}가 아니면 검사 없이 통과되어야 한다.
     *
     * @throws Exception 응답 쓰기 중 오류 발생 시
     */
    @Test
    void preHandle_nonHandlerMethod_passes() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(GATEWAY_HEADER, "true");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
    }

    /**
     * {@link TestController}의 지정한 메서드에 대한 실제 {@link HandlerMethod}를 생성한다.
     *
     * @param methodName 핸들러 메서드명
     * @return 생성된 {@link HandlerMethod}
     * @throws NoSuchMethodException 메서드를 찾지 못한 경우
     */
    private HandlerMethod handlerFor(String methodName) throws NoSuchMethodException {
        TestController controller = new TestController();
        Method method = TestController.class.getDeclaredMethod(methodName);
        return new HandlerMethod(controller, method);
    }

    /**
     * HandlerMethod 생성을 위한 더미 컨트롤러.
     */
    static class TestController {

        /**
         * {@link InternalApi}가 선언된 내부 전용 핸들러.
         */
        @InternalApi
        public void internalEndpoint() {
        }

        /**
         * 어노테이션이 없는 일반 핸들러.
         */
        public void publicEndpoint() {
        }
    }
}
