# 개발 원칙

## 코드 스타일
- Java: Google Java Style Guide 준수
- Lombok 적극 활용 (`@Getter`, `@Builder`, `@RequiredArgsConstructor`)
- 불필요한 abstraction 금지 — 현재 요구사항만 구현

## JavaDoc 작성 규칙 (필수)
- **모든 클래스, 메서드, 필드**에 JavaDoc(`/** ... */`) 형식으로 **한글** 주석을 작성한다
- 클래스: 역할과 주요 동작 설명
- public/private 메서드: `@param`, `@return`, `@throws` 태그 포함
- 필드: 한 줄 JavaDoc(`/** 설명. */`)으로 의미 설명
- 테스트 클래스와 테스트 메서드도 예외 없이 JavaDoc 작성
- 인라인 주석(`//`)은 WHY가 비명확한 곳에 추가로 작성 가능

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
