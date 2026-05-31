# MSA Commerce

MSA 아키텍처와 Apache Kafka 이벤트 드리븐 패턴을 학습하기 위해 만든 샘플 커머스 프로젝트입니다.

---

## 기술 스택

| 구분          | 기술                                            |
|-------------|---------------------------------------------------|
| 백엔드 | Spring Boot 3.x, Java 21, Gradle (Kotlin DSL) |
| API Gateway | Spring Cloud Gateway 4.x |
| 인증 | JWT |
| ORM | Spring Data JPA |
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
├── api-gateway/        # Spring Cloud Gateway — JWT 검증, 라우팅
├── auth-service/       # JWT 발급 서비스
├── order-service/      # 주문 서비스
├── payment-service/    # 결제 서비스 — 시뮬레이션
├── stock-service/      # 재고 서비스
├── common-module       # 서비스에서 공통으로 사용하는 코드
├── frontend/           # React 앱
└── docker-compose.yml
```
