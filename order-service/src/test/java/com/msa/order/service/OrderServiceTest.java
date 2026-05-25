package com.msa.order.service;

import com.msa.order.domain.Order;
import com.msa.order.domain.OrderStatus;
import com.msa.order.dto.CreateOrderRequest;
import com.msa.order.dto.OrderResponse;
import com.msa.order.kafka.producer.OrderEventProducer;
import com.msa.order.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * OrderService 단위 테스트.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 단위 테스트")
class OrderServiceTest {

    /**
     * 테스트 대상 OrderService.
     */
    @InjectMocks
    private OrderService orderService;

    /**
     * 목킹된 OrderRepository.
     */
    @Mock
    private OrderRepository orderRepository;

    /**
     * 목킹된 OrderEventProducer.
     */
    @Mock
    private OrderEventProducer orderEventProducer;

    /**
     * 주문 생성 성공 테스트.
     */
    @Test
    @DisplayName("createOrder: 주문 생성 성공 시 저장 및 이벤트 발행 호출 검증")
    void createOrder_주문생성_성공() {
        // given
        CreateOrderRequest request =
            new CreateOrderRequest(1L, 2, new BigDecimal("20000.00"));

        Order savedOrder =
            Order.builder()
                .userId("test")
                .productId(1L)
                .quantity(2)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("20000.00"))
                .createdAt(LocalDateTime.now())
                .build();
        // 리플렉션으로 id 설정 (JPA 자동 생성 시뮬레이션)
        ReflectionTestUtils.setField(savedOrder, "id", 1L);

        given(orderRepository.save(any(Order.class))).willReturn(savedOrder);

        // when
        OrderResponse response = orderService.createOrder(request, "test");

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.userId()).isEqualTo("test");
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);

        then(orderRepository).should().save(any(Order.class));
        then(orderEventProducer).should().sendOrderCreated(any());
    }

    /**
     * 존재하지 않는 주문 조회 시 예외 발생 테스트.
     */
    @Test
    @DisplayName("getOrder: 존재하지 않는 주문 조회 시 NoSuchElementException 발생")
    void getOrder_존재하지않는주문_예외발생() {
        // given
        given(orderRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.getOrder(999L))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessageContaining("999");
    }

    /**
     * payment.failed 이벤트 처리 시 주문 취소 테스트.
     */
    @Test
    @DisplayName("cancelOrder: payment.failed 이벤트 처리 시 주문 상태 CANCELLED로 변경")
    void handlePaymentFailed_주문취소() {
        // given
        Order order =
            Order.builder()
                .userId("user1")
                .productId(1L)
                .quantity(2)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("20000.00"))
                .createdAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(order, "id", 1L);

        given(orderRepository.findById(1L)).willReturn(Optional.of(order));

        // when
        orderService.cancelOrder(1L);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    /**
     * stock.reserved 이벤트 처리 시 주문 완료 테스트.
     */
    @Test
    @DisplayName("completeOrder: stock.reserved 이벤트 처리 시 주문 상태 COMPLETED로 변경")
    void handleStockReserved_주문완료() {
        // given
        Order order =
            Order.builder()
                .userId("user1")
                .productId(1L)
                .quantity(2)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("20000.00"))
                .createdAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(order, "id", 1L);

        given(orderRepository.findById(1L)).willReturn(Optional.of(order));

        // when
        orderService.completeOrder(1L);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }
}
