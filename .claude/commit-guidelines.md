# 커밋 메시지 양식

## 형식
```
<type>(<scope>): <subject>

<body>

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
```

## type

| type | 설명 |
|------|------|
| `feat` | 새 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 기능 변경 없는 코드 개선 |
| `test` | 테스트 코드 추가·수정 |
| `docs` | 문서 작성·수정 |
| `chore` | 빌드 설정, 의존성, CI/CD 등 |
| `style` | 코드 포맷, 코드 스타일만 변경 |

## scope
서비스 단위로 지정한다: `auth`, `gateway`, `order`, `payment`, `stock`, `frontend`, `infra`, `ci`

## subject 규칙
- 한글로 작성, 명령형 (~함, ~추가, ~수정)
- 50자 이내
- 마침표 없음

## body 규칙
- 변경 이유나 배경이 명확하지 않은 경우에만 작성
- 생략 가능

## 예시
```
feat(order): 주문 생성 API 및 order.created 이벤트 발행 구현

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
```
```
fix(payment): order.created 컨슈머 중복 처리 방지 로직 추가

eventId 기반 멱등성 체크가 누락되어 동일 이벤트 재처리 시
결제가 중복 생성되는 문제를 수정함.

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
```
