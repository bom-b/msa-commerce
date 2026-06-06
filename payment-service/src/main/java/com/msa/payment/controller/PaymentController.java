package com.msa.payment.controller;

import com.msa.common.auth.annotation.Authenticated;
import com.msa.common.auth.annotation.CurrentUser;
import com.msa.payment.dto.PaymentResponse;
import com.msa.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 결제 REST 컨트롤러.
 */
@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    /** 결제 비즈니스 로직 서비스. */
    private final PaymentService paymentService;

    /**
     * 주문 ID로 본인 소유의 결제를 조회한다.
     *
     * @param userId  X-User-Id 헤더에서 추출한 인증된 사용자 ID
     * @param orderId 조회할 주문 ID
     * @return 200 OK + 결제 응답 DTO
     */
    @Authenticated
    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentResponse> getPayment(@CurrentUser Long userId, @PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId, userId));
    }
}
