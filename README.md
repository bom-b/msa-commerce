# MSA Commerce

MSA 아키텍처와 Apache Kafka 이벤트 드리븐 패턴을 학습하기 위해 만든 샘플 커머스 프로젝트입니다.

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| 백엔드 | Spring Boot 3.x, Java 21, Gradle (Kotlin DSL) |
| API Gateway | Spring Cloud Gateway 4.x |
| 인증 | JWT (jjwt), HS256 |
| ORM | Spring Data JPA, Hibernate |
| DB | PostgreSQL 15 |
| 메시지 브로커 | Apache Kafka 3.x |
| 프론트엔드 | React 18, Axios |
| 인프라 | Docker, Docker Compose |
| CI/CD | GitHub Actions, GHCR |
| 테스트 | JUnit 5, Mockito, Testcontainers |

---

## 프로젝트 구조

```
msa-commerce/
├── api-gateway/        # Spring Cloud Gateway — JWT 검증, 라우팅 (포트 8080)
├── auth-service/       # JWT 발급 서비스 (포트 8081)
├── order-service/      # 주문 서비스 (포트 8082)
├── payment-service/    # 결제 서비스 — 시뮬레이션 (포트 8083)
├── stock-service/      # 재고 서비스 (포트 8084)
├── frontend/           # React 앱 (포트 3000)
├── docker-compose.yml
└── docker-compose.dev.yml
```

### 서비스 간 통신 (Kafka Choreography Saga)

```
주문 생성 → [order.created] → 결제 처리 → [payment.completed] → 재고 차감 → [stock.reserved] → 주문 완료
                                          → [payment.failed]   → 주문 취소
                                                                → [stock.insufficient] → 결제 환불 → 주문 취소
```

각 서비스는 독립된 PostgreSQL DB를 사용하며, 서비스 간 직접 REST 호출 없이 Kafka 이벤트로만 통신합니다.
