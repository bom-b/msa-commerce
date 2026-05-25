# AI Agent 워크플로우 (필수)

## 코딩 작업 분리 원칙

모든 코딩 작업은 반드시 두 단계로 분리하여 수행한다. 작업 대상에 따라 에이전트를 선택한다:

### 백엔드 작업 (order-service, payment-service, stock-service, auth-service, api-gateway)

1. **backend-code-writer 에이전트**: 코드 구현 (`.claude/agents/backend-code-writer.md`)
2. **backend-code-reviewer 에이전트**: 구현된 코드 검토 (`.claude/agents/backend-code-reviewer.md`)

### 프론트엔드 작업 (frontend/)

1. **frontend-code-writer 에이전트**: 코드 구현 (`.claude/agents/frontend-code-writer.md`)
    - 작업 시작 전 `.claude/frontend-design-system.md`를 반드시 읽어 디자인 시스템을 파악한다
2. **frontend-code-reviewer 에이전트**: 구현된 코드 검토 (`.claude/agents/frontend-code-reviewer.md`)
    - 디자인 시스템 준수 여부를 포함하여 검토한다

단순 질문, 파일 탐색, 설명 요청 등 코드를 작성하지 않는 작업은 이 워크플로우를 적용하지 않는다.

## 테스트 실행 규칙

- 테스트 코드는 구현 시 항상 작성한다
- **테스트 실행은 사용자가 명시적으로 요청한 경우에만 수행한다**
- 사용자가 "테스트해줘", "테스트 실행", "test run" 등을 직접 요청하지 않으면 테스트를 실행하지 않는다
