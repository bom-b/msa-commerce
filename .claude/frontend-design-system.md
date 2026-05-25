---
name: design-system
description: "프론트엔드의 전체 디자인 시스템"
metadata:
    node_type: memory
    type: project
    originSessionId: 56cb83e9-c28f-4f0c-b005-20d954c20499
---

# 프론트엔드 디자인 시스템

스택: React + TypeScript + Vite + SCSS Modules

---

## 1. 색상 팔레트 (`_variables.scss`)

```scss
$color-primary: #000; // 검정 — 주요 강조, 버튼, 날짜 텍스트
$color-secondary: #aeaeae; // 중간 회색 — 아이콘, 보조 버튼
$color-bg: #fff; // 흰색 — 카드, 헤더, 모달 배경
$color-text: #1a1a1a; // 거의 검정 — 본문 텍스트
$color-text-secondary: #888; // 중간 회색 — 플레이스홀더, 부제목
$color-border: #e0e0e0; // 연한 회색 — 구분선, 인풋 하단선
```

**디자인 철학**: 흑백 미니멀리즘. 색상 강조 없이 명도 대비만으로 계층 표현.

---

## 2. 타이포그래피 (`_variables.scss`)

**폰트 로드** (`global.scss`):

```scss
@font-face {
    font-family: 'Pretendard Variable';
    src: url('../assets/font/PretendardVariable.woff2') format('woff2-variations');
    font-weight: 100 900;
    font-style: normal;
    font-display: swap;
}
```

폰트 파일은 `src/assets/font/PretendardVariable.woff2`에 로컬 번들링.

## 3. 아이콘 시스템

웬만한 아이콘은 SVG로 만든다.
SVG를 React 컴포넌트로 import (`?react` suffix, Vite):

```tsx
import SearchIcon from '../assets/icon/magnifying-glass.svg?react'
// 사용
<
SearchIcon
width = {22}
height = {22}
/>
```

---

## 4. 주요 서드파티 라이브러리

| 라이브러리                   | 용도                |
|-------------------------|-------------------|
| `@tanstack/react-query` | 서버 상태 관리          |
| `zustand`               | 전역 상태 (userStore) |
| `react-router-dom`      | URL 기반 라우팅        |

---
