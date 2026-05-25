package com.msa.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.order.domain.Order;
import com.msa.order.domain.OrderStatus;
import com.msa.order.dto.CreateOrderRequest;
import com.msa.order.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OrderController 통합 테스트.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@EmbeddedKafka(
    partitions = 1,
    topics = {
        "order.created",
        "payment.completed",
        "payment.failed",
        "stock.reserved",
        "stock.insufficient"
    })
@DisplayName("OrderController 통합 테스트")
class OrderControllerTest {

    /**
     * Testcontainers PostgreSQL 컨테이너 (클래스 공유).
     */
    @Container
    static final PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("order_db")
            .withUsername("postgres")
            .withPassword("postgres");

    /**
     * MockMvc HTTP 요청 수행 객체.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * JSON 직렬화/역직렬화 ObjectMapper.
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 테스트용 주문 레포지토리.
     */
    @Autowired
    private OrderRepository orderRepository;

    /**
     * Testcontainers 컨테이너 속성을 Spring 컨텍스트에 동적으로 등록한다.
     *
     * @param registry 동적 속성 레지스트리
     */
    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // EmbeddedKafka의 브로커 주소를 주입
        registry.add(
            "spring.kafka.bootstrap-servers",
            () -> System.getProperty("spring.embedded.kafka.brokers", "localhost:9092"));
    }

    /**
     * 각 테스트 후 DB 데이터를 초기화한다.
     */
    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
    }

    /**
     * POST /orders 주문 생성 성공 테스트.
     *
     * @throws Exception MockMvc 요청 수행 시 발생할 수 있는 예외
     */
    @Test
    @DisplayName("POST /orders: 주문 생성 성공 시 201 응답 및 orderId 반환")
    void POST_orders_주문생성_성공() throws Exception {
        // given
        CreateOrderRequest request = new CreateOrderRequest(1L, 2, new BigDecimal("20000.00"));

        // when & then
        mockMvc
            .perform(
                post("/orders")
                    .header("X-User-Id", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.userId", is(1)))
            .andExpect(jsonPath("$.productId", is(1)))
            .andExpect(jsonPath("$.quantity", is(2)))
            .andExpect(jsonPath("$.status", is("PENDING")));
    }

    /**
     * POST /orders X-User-Id 헤더 누락 시 401 응답 테스트.
     *
     * @throws Exception MockMvc 요청 수행 시 발생할 수 있는 예외
     */
    @Test
    @DisplayName("POST /orders: X-User-Id 헤더 누락 시 401 응답")
    void POST_orders_XUserId헤더_누락_401() throws Exception {
        // given
        CreateOrderRequest request = new CreateOrderRequest(1L, 2, new BigDecimal("20000.00"));

        // when & then
        mockMvc
            .perform(
                post("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    /**
     * GET /orders/{id} 주문 단건 조회 성공 테스트.
     *
     * @throws Exception MockMvc 요청 수행 시 발생할 수 있는 예외
     */
    @Test
    @DisplayName("GET /orders/{id}: 존재하는 주문 조회 시 200 응답 및 주문 정보 반환")
    void GET_orders_id_주문조회_성공() throws Exception {
        // given
        Order saved =
            orderRepository.save(
                Order.builder()
                    .userId(1L)
                    .productId(1L)
                    .quantity(2)
                    .status(OrderStatus.PENDING)
                    .totalAmount(new BigDecimal("20000.00"))
                    .createdAt(LocalDateTime.now())
                    .build());

        // when & then
        mockMvc
            .perform(get("/orders/{id}", saved.getId()).header("X-User-Id", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
            .andExpect(jsonPath("$.userId", is(1)))
            .andExpect(jsonPath("$.status", is("PENDING")));
    }

    /**
     * GET /orders/{id} 존재하지 않는 주문 조회 시 404 테스트.
     *
     * @throws Exception MockMvc 요청 수행 시 발생할 수 있는 예외
     */
    @Test
    @DisplayName("GET /orders/{id}: 존재하지 않는 주문 조회 시 404 응답")
    void GET_orders_id_존재하지않는주문_404() throws Exception {
        // when & then
        mockMvc
            .perform(get("/orders/{id}", 999L).header("X-User-Id", "1"))
            .andExpect(status().isNotFound());
    }

    /**
     * GET /orders X-User-Id 헤더 누락 시 401 응답 테스트.
     *
     * @throws Exception MockMvc 요청 수행 시 발생할 수 있는 예외
     */
    @Test
    @DisplayName("GET /orders: X-User-Id 헤더 누락 시 401 응답")
    void GET_orders_XUserId헤더_누락_401() throws Exception {
        // when & then
        mockMvc.perform(get("/orders")).andExpect(status().isUnauthorized());
    }
}
