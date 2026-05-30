package com.msa.stock.service;

import com.msa.common.event.*;
import com.msa.stock.domain.Stock;
import com.msa.stock.domain.StockReservation;
import com.msa.stock.dto.StockResponse;
import com.msa.stock.repository.StockRepository;
import com.msa.stock.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * 재고 비즈니스 로직 서비스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

    /**
     * 재고 데이터 접근 레포지토리.
     */
    private final StockRepository stockRepository;

    /**
     * 재고 예약 데이터 접근 레포지토리.
     */
    private final StockReservationRepository stockReservationRepository;

    /**
     * 스프링 애플리케이션 이벤트 발행자.
     */
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 전체 재고 목록을 조회한다.
     *
     * @return 재고 응답 DTO 목록
     */
    public List<StockResponse> findAll() {
        return stockRepository.findAllWithProduct().stream()
            .map(StockResponse::from)
            .toList();
    }

    /**
     * 상품 ID로 재고를 조회한다.
     *
     * @param productId 상품 ID
     * @return 재고 응답 DTO
     * @throws NoSuchElementException 해당 상품의 재고가 없을 경우
     */
    public StockResponse findByProductId(Long productId) {
        Stock stock = stockRepository.findByProduct_Id(productId)
            .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));
        return StockResponse.from(stock);
    }

    /**
     * 주문 생성 이벤트를 처리하여 재고를 예약한다.
     * 예약 성공 시 커밋 후 stock.reserved 이벤트를 발행하고,
     * 재고 부족 시 stock.insufficient 이벤트를 즉시 발행한다.
     *
     * @param event 주문 생성 이벤트
     */
    @Transactional
    public void reserveStock(OrderCreatedEvent event) {
        Stock stock = stockRepository.findByProductIdWithProduct(event.productId())
            .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다. productId: " + event.productId()));

        try {
            StockReservation reservation = StockReservation.createReservation(stock, event.orderId(), event.quantity());
            stockReservationRepository.save(reservation);
            log.info("재고 예약 완료 - orderId: {}, productId: {}, quantity: {}", event.orderId(), event.productId(), event.quantity());

            BigDecimal totalAmount = stock.getProduct().getPrice().multiply(BigDecimal.valueOf(event.quantity()));
            StockReservedEvent reservedEvent = new StockReservedEvent(UUID.randomUUID(), event.orderId(), event.userId(), event.productId(), event.quantity(), totalAmount);
            eventPublisher.publishEvent(reservedEvent);

        } catch (IllegalArgumentException e) {
            log.warn("재고 부족 - orderId: {}, productId: {}, reason: {}", event.orderId(), event.productId(), e.getMessage());
            StockInsufficientEvent insufficientEvent = new StockInsufficientEvent(UUID.randomUUID(), event.orderId(), event.productId(), event.quantity(), e.getMessage());
            eventPublisher.publishEvent(insufficientEvent);
        }
    }

    /**
     * 결제 완료 이벤트를 처리하여 재고 예약을 확정한다.
     *
     * @param event 결제 완료 이벤트
     */
    @Transactional
    public void confirmReservation(PaymentCompletedEvent event) {
        StockReservation reservation = stockReservationRepository.findByOrderId(event.orderId())
            .orElseThrow(() -> new NoSuchElementException("재고 예약을 찾을 수 없습니다. orderId: " + event.orderId()));

        reservation.confirm();
        log.info("재고 예약 확정 - orderId: {}, quantity: {}", event.orderId(), reservation.getReservedQuantity());
    }

    /**
     * 결제 실패 이벤트를 처리하여 재고 예약을 취소한다.
     *
     * @param event 결제 실패 이벤트
     */
    @Transactional
    public void releaseReservation(PaymentFailedEvent event) {
        StockReservation reservation = stockReservationRepository.findByOrderId(event.orderId())
            .orElseThrow(() -> new NoSuchElementException("재고 예약을 찾을 수 없습니다. orderId: " + event.orderId()));

        reservation.cancel();
        log.info("재고 예약 취소 - orderId: {}, quantity: {}", event.orderId(), reservation.getReservedQuantity());
    }

}
