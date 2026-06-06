package com.msa.payment.service;

import com.msa.common.event.PaymentCompletedEvent;
import com.msa.common.event.PaymentFailedEvent;
import com.msa.common.event.StockReservedEvent;
import com.msa.payment.client.AuthServiceClient;
import com.msa.payment.domain.Payment;
import com.msa.payment.domain.PaymentStatus;
import com.msa.payment.dto.PaymentResponse;
import com.msa.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * 결제 비즈니스 로직 서비스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthServiceClient authServiceClient;

    /**
     * stock.reserved 이벤트를 처리하여 예치금을 차감하고 결제 결과 이벤트를 발행한다.
     *
     * @param event 재고 확보 완료 이벤트
     */
    @Transactional
    public void processPayment(StockReservedEvent event) {
        if (paymentRepository.existsByOrderId(event.orderId())) {
            log.warn("이미 처리된 주문 - orderId: {}", event.orderId());
            return;
        }

        Payment payment = Payment.builder()
            .orderId(event.orderId())
            .userId(event.userId())
            .amount(event.totalAmount())
            .status(PaymentStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();
        Payment saved = paymentRepository.saveAndFlush(payment);

        try {
            authServiceClient.deduct(event.userId(), event.totalAmount());

            saved.updateStatus(PaymentStatus.COMPLETED);
            log.info("결제 완료 - paymentId: {}, orderId: {}", saved.getId(), saved.getOrderId());
            eventPublisher.publishEvent(new PaymentCompletedEvent(UUID.randomUUID(), saved.getOrderId(), saved.getId(), saved.getAmount()));

        } catch (IllegalArgumentException e) {
            saved.updateStatus(PaymentStatus.FAILED);
            log.warn("결제 실패 - orderId: {}, reason: {}", event.orderId(), e.getMessage());
            eventPublisher.publishEvent(new PaymentFailedEvent(UUID.randomUUID(), saved.getOrderId(), saved.getId(), saved.getAmount(), e.getMessage()));
        }
    }

    /**
     * 주문 ID로 요청자 본인 소유의 결제를 조회한다.
     *
     * @param orderId 조회할 주문 ID
     * @param userId  인증된 사용자 ID
     * @return 결제 응답 DTO
     * @throws NoSuchElementException 본인 소유의 해당 주문 ID 결제가 없을 경우
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId, Long userId) {
        Payment payment = paymentRepository.findByOrderIdAndUserId(orderId, userId)
            .orElseThrow(() -> new NoSuchElementException("결제를 찾을 수 없습니다. orderId: " + orderId));
        return PaymentResponse.from(payment);
    }
}
