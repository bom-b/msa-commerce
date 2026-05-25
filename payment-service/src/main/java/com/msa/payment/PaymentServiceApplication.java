package com.msa.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Payment Service 애플리케이션 진입점. 포트 8083에서 동작하며, order.created 이벤트를 구독하여
 * 결제를 처리하고 payment.completed 이벤트를 발행한다.
 */
@SpringBootApplication
public class PaymentServiceApplication {

    /**
     * 애플리케이션을 시작한다.
     *
     * @param args 실행 인수
     */
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
