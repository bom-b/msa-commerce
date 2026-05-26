# 개발 원칙

## 코드 스타일

- Lombok 적극 활용 (`@Getter`, `@Builder`, `@RequiredArgsConstructor`)
- 불필요한 abstraction 금지 — 현재 요구사항만 구현

## 포매팅 규칙 (필수)

- **들여쓰기**: 스페이스 4칸 (탭 사용 금지)
- **줄 길이**: 150자 이하. 150자 미만이면 줄바꿈하지 않는다
- **줄바꿈 기준**: 메서드 체이닝, 파라미터 목록, 조건식이 150자를 넘을 때만 줄바꿈
- **빈 줄**: 관련 없는 블록 사이에만 삽입, 과도한 공백 금지
- `.editorconfig` 설정을 항상 따른다 (프로젝트 루트 참고)

## JavaDoc 작성 규칙 (필수)

- **모든 클래스, 메서드, 필드**에 JavaDoc(`/** ... */`) 형식으로 **한글** 주석을 작성한다
- 클래스: 역할과 주요 동작 설명
- public/private 메서드: `@param`, `@return`, `@throws` 태그 포함
- 필드: 한 줄 JavaDoc(`/** 설명. */`)으로 의미 설명
- 테스트 클래스와 테스트 메서드도 예외 없이 JavaDoc 작성
- 인라인 주석(`//`)은 WHY가 비명확한 곳에 추가로 작성 가능

### JavaDoc 금지 패턴

**독자에게 설명하는 문체 금지.** JavaDoc은 독자를 가르치는 문서가 아니다. `<p>` 단락으로 동작 원리, 보안 이유, 설계 의도를 풀어쓰지 않는다.

```java
// 나쁜 예 — 가르치듯 설명하는 JavaDoc
/**
 * 주문을 생성한다.
 *
 * <p>userId는 클라이언트 요청 본문 대신 API Gateway가 JWT 검증 후 설정한
 * {@code X-User-Id} 헤더에서 가져온다. 이를 통해 클라이언트가 임의의 userId를
 * 요청 본문에 담아 보내는 보안 취약점을 차단한다.
 *
 * @param userId API Gateway가 JWT에서 추출하여 설정한 인증된 사용자 ID
 */

// 좋은 예 — @param에 사실만 간결하게
/**
 * 주문을 생성한다.
 *
 * @param userId  X-User-Id 헤더에서 추출한 사용자 ID
 * @param request 주문 생성 요청 DTO
 * @return 201 Created + 생성된 주문 응답 DTO
 */
```

**기준**: `@param`, `@return`, `@throws` 태그 외에 추가 단락(`<p>`)이 필요하다면, 그 내용이 코드를 읽어서는 절대 알 수 없는 외부 제약이나 히든 인변식인지 먼저 물어볼 것. 단순히 "왜 이렇게 구현했는지" 설명하는 내용은 삭제한다.

## 예외 처리 원칙

- **컨트롤러 try-catch 금지**: `GlobalExceptionHandler`가 전역으로 처리하므로, 컨트롤러에서
  `NoSuchElementException`, `MethodArgumentNotValidException` 등 비즈니스 예외를 개별
  try-catch하지 않는다. 예외는 그대로 throw하면 핸들러가 HTTP 응답으로 변환한다.
- **서비스 계층**: 비즈니스 규칙 위반은 `NoSuchElementException` 또는 `IllegalArgumentException`을
  throw한다. 직접 HTTP 상태를 결정하지 않는다.
- **Kafka 컨슈머**: 멱등성 보장용 중복 처리는 컨슈머 내부에서 직접 처리한다
  (HTTP 레이어가 아니므로 GlobalExceptionHandler 적용 대상 아님).

## Spring Boot 서비스 공통 구조

```
src/main/java/com/msa/{service}/
├── {Service}Application.java
├── config/          # Kafka, Security 설정
├── controller/      # REST 컨트롤러
├── service/         # 비즈니스 로직
├── domain/          # JPA 엔티티
├── repository/      # Spring Data Repository
├── kafka/
│   ├── producer/    # Kafka 발행자
│   └── consumer/    # Kafka 구독자
└── dto/             # 요청/응답 + Kafka 이벤트 DTO
```

## Kafka 이벤트 DTO 규칙

- 이벤트 클래스명: `{Action}Event` (예: `OrderCreatedEvent`)
- 모든 이벤트에 `eventId` (UUID) 포함 — 멱등성 처리용
- JSON 직렬화: Jackson

## 테스트 전략

- **단위 테스트**: Service 계층 (Mockito로 의존성 목킹)
- **통합 테스트**: Controller 계층 (`@SpringBootTest` + MockMvc)
- **Kafka 테스트**: `@EmbeddedKafka` 또는 Testcontainers Kafka
- **DB 테스트**: Testcontainers PostgreSQL
- 각 서비스별 최소 80% 커버리지 목표

## 보안 원칙

- JWT Secret은 환경변수로 관리 (`JWT_SECRET`)
- DB 패스워드는 환경변수로 관리
- 소스코드에 민감 정보 하드코딩 금지 (test/test 인증정보 제외)
- SQL Injection 방지: JPA 파라미터 바인딩 사용

## DB 설계 원칙

### FK 관계 및 정규화 (필수)

**같은 서비스 내 엔티티 간 관계**
- 같은 서비스 DB 내에서 연관된 엔티티는 `@OneToOne`, `@ManyToOne`, `@OneToMany` 등 JPA 관계 어노테이션으로 반드시 매핑한다
- `Long userId` 처럼 단순 ID 필드로만 타 엔티티를 참조하는 방식은 같은 서비스 내에서 금지한다 — JPA 관계 매핑으로 DB 레벨 FK 제약과 정합성을 함께 보장한다
- FetchType은 기본적으로 **LAZY**를 사용한다 (`fetch = FetchType.LAZY`)

**서비스 간 데이터 참조**
- 서비스 간에는 DB를 공유하지 않으므로, 타 서비스 엔티티를 JPA로 직접 참조하는 것은 금지한다
- 타 서비스 데이터는 ID(Long)만 컬럼으로 저장한다 (예: `orderId`, `productId`)

**새 엔티티 설계 시 체크리스트**
- 이 필드가 같은 서비스의 다른 엔티티에 이미 존재하는 데이터인가? → JPA 관계 매핑으로 참조
- 이 테이블의 컬럼이 반복·중복되는 데이터인가? → 별도 테이블로 분리 (3NF)
- 관계의 주인(FK를 가진 쪽)은 어디인가? → 비즈니스상 "의존하는" 쪽이 FK를 보유

**관계 유형별 어노테이션**

| 관계 | 어노테이션 | FK 위치 |
|------|-----------|---------|
| 1:1  | `@OneToOne(fetch = FetchType.LAZY)` + `@JoinColumn` | 의존하는 쪽 |
| N:1  | `@ManyToOne(fetch = FetchType.LAZY)` + `@JoinColumn` | N 쪽 |
| 1:N  | `@OneToMany(mappedBy = "필드명")` | 컬렉션 쪽엔 FK 없음 |

- 역방향 탐색(`mappedBy`)은 실제로 필요한 경우에만 추가한다 — 불필요한 양방향 매핑 금지
- Spring Data JPA에서 관계 필드로 조회 시 property traversal 문법을 사용한다 (예: `findByUser_Id(Long userId)` → `user.id` 탐색)
