# MSA Commerce - AI Agent 개발 가이드라인

@.claude/agent-workflow.md
@.claude/commit-guidelines.md
@.claude/coding-standards.md

---

## 프로젝트 개요

Docker + Spring Boot + React + Postgres + Kafka 기반 MSA 학습 프로젝트.
**학습 목적**: Kafka 이벤트 드리븐 아키텍처와 MSA 패턴 이해가 핵심 목표.

---

## 레포지토리 구조 (Monorepo)

```
msa-commerce/
├── api-gateway/          # Spring Cloud Gateway (포트 8080)
├── auth-service/         # JWT 발급 서비스 (포트 8081)
├── order-service/        # 주문 서비스 (포트 8082)
├── payment-service/      # 결제 서비스 (포트 8083)
├── stock-service/        # 재고 서비스 (포트 8084)
├── frontend/             # React 앱 (포트 3000)
├── docker-compose.yml    # 전체 인프라 + 서비스 정의
├── docker-compose.dev.yml # 로컬 개발용 (인프라만)
├── .github/
│   └── workflows/
│       ├── ci.yml        # PR 빌드/테스트
│       └── cd.yml        # main 브랜치 배포
└── CLAUDE.md
```

---

## 기술 스택

| 구성요소        | 기술                               |
|-------------|----------------------------------|
| API Gateway | Spring Cloud Gateway 4.x         |
| 백엔드 서비스     | Spring Boot 3.x, Java 21         |
| 빌드 도구       | Gradle (Kotlin DSL)              |
| ORM         | Spring Data JPA + Hibernate      |
| DB          | PostgreSQL 15 (서비스별 독립 DB)       |
| 메시지 브로커     | Apache Kafka 3.x                 |
| 인증          | JWT (jjwt 라이브러리)                 |
| 프론트엔드       | React 18, Axios                  |
| 컨테이너        | Docker, Docker Compose           |
| CI/CD       | GitHub Actions                   |
| 테스트         | JUnit 5, Mockito, Testcontainers |

---

## 포트 할당

| 서비스              | 포트   |
|------------------|------|
| Frontend (React) | 3000 |
| API Gateway      | 8080 |
| Auth Service     | 8081 |
| Order Service    | 8082 |
| Payment Service  | 8083 |
| Stock Service    | 8084 |
| PostgreSQL       | 5432 |
| Kafka            | 9092 |
| Kafka UI         | 8090 |

---

## 아키텍처 설계

### 서비스 간 통신 흐름

```
[Browser] → [Frontend :3000]
                ↓ REST
          [API Gateway :8080]
           ├─→ [Auth Service :8081]   (POST /auth/login)
           ├─→ [Order Service :8082]  (주문 CRUD)
           ├─→ [Payment Service :8083] (결제 조회)
           └─→ [Stock Service :8084]  (재고 조회)

Kafka Choreography-based Saga:
Order → [order.created] → Stock → [stock.reserved]     → Payment → [payment.completed] → Order(COMPLETED)
                                                                                        → Stock(CONFIRMED)
                               → [stock.insufficient] → Order(CANCELLED)
                        Payment → [payment.failed]    → Order(CANCELLED)
                                                      → Stock(RELEASE)
```

### Kafka 토픽 목록

| 토픽명                  | 발행자             | 구독자                       | 설명                      |
|----------------------|-----------------|-----------------------------|-------------------------|
| `order.created`      | Order Service   | Stock Service               | 주문 생성, 재고 예약 트리거        |
| `stock.reserved`     | Stock Service   | Payment Service             | 재고 확보 완료, 결제 처리 트리거     |
| `stock.insufficient` | Stock Service   | Order Service               | 재고 부족, 주문 취소            |
| `payment.completed`  | Payment Service | Order Service, Stock Service | 결제 완료, 주문 완료 + 재고 확정 트리거 |
| `payment.failed`     | Payment Service | Order Service, Stock Service | 결제 실패, 주문 취소 + 재고 복구 트리거 |

### 주문 상태 전이

```
PENDING → (payment.completed) → COMPLETED
        → (payment.failed)    → CANCELLED
        → (stock.insufficient) → CANCELLED
```

---

## 각 서비스 상세 설계

### 1. API Gateway (:8080)

- **역할**: 단일 진입점, JWT 검증, 라우팅
- **JWT 필터**: Authorization 헤더 검증 → 각 서비스로 라우팅
- 
### 2. Auth Service (:8081)

- **역할**: JWT 발급 + 사용자 예치금 관리
- **DB**: `auth_db` (PostgreSQL)

### 3. Order Service (:8082)

- **역할**: 주문 생성, 주문 상태 관리
- **DB**: `order_db` (PostgreSQL)

### 4. Payment Service (:8083)

- **역할**: 결제 처리
- **DB**: `payment_db` (PostgreSQL)

### 5. Stock Service (:8084)

- **역할**: 상품 관리, 재고 관리
- **DB**: `stock_db` (PostgreSQL)

### 6. Frontend (:3000)

- **페이지 구성**:
    - 로그인 페이지 (`/login`)
    - 상품/재고 목록 + 주문 생성 (`/`)
    - 내 주문 목록 (`/orders`)
    - 주문 상세 + 결제 상태 (`/orders/:id`)
    - 재고 관리 (`/inventory`)
    - 예치금 충전 (`/charge`)
- **인증**: JWT를 localStorage에 저장, Axios 인터셉터로 자동 첨부

---

## Docker & 인프라

### docker-compose.yml 구조

```yaml
services:
    kafka:         # 메시지 브로커 (KRaft 모드, Zookeeper 미사용)
    kafka-ui:      # Kafka 모니터링 (Provectuslabs)
    postgres:      # 단일 PostgreSQL 인스턴스 (DB 분리)
    auth-service:
    api-gateway:
    order-service:
    payment-service:
    stock-service:
    frontend:
    nginx:         # 내부 리버스 프록시
    cloudflared:   # Cloudflare Tunnel (외부 진입점, host 포트 미노출)
```

### DB 분리 방식

단일 PostgreSQL 컨테이너 내 DB 분리:

- `auth_db`
- `order_db`
- `payment_db`
- `stock_db`

### 환경변수 관리

- `.env` 파일로 관리 (`.gitignore`에 추가)
- `.env.example` 파일 제공
- 각 서비스 `application.yml`에서 환경변수 참조

---

## CI/CD 파이프라인

### GitHub Actions 구성

**ci.yml** (PR 트리거):

1. 변경된 서비스만 감지 (path filter)
2. 해당 서비스 Gradle 빌드
3. 단위 테스트 실행
4. Docker 이미지 빌드 검증

**cd.yml** (main 브랜치 push 트리거):

1. 변경된 서비스 감지
2. Docker 이미지 빌드 + GHCR(GitHub Container Registry) 푸시
3. 서버 SSH 접속 → `docker-compose pull + up -d`

### 이미지 태깅 전략

- `ghcr.io/{owner}/msa-commerce/{service}:latest`
- `ghcr.io/{owner}/msa-commerce/{service}:{git-sha}`

---

## 주의사항 및 제약

1. **Kubernetes 미사용** — Docker Compose로만 배포
2. **Auth Service 단순화** — 회원가입 없음, test/test 고정
3. **실제 PG 연동 없음** — 예치금(UserBalance) 차감으로 결제 처리, 외부 카드사 연동 없음
4. **기능 최소화** — Kafka + MSA 패턴 학습이 목적, CRUD는 최소한으로
5. **각 서비스 독립 DB** — 서비스 간 DB 공유 절대 금지
6. **동기 호출 최소화** — 서비스 간 직접 REST 호출 대신 Kafka 이벤트 사용 (예외: Payment → Auth 예치금 차감)
