# DDD Gym - 주차별 TODO

> 이 문서는 각 주차에서 해야 할 작업을 명확하게 정리한 것입니다.
> 참가자는 `participants/sample` 폴더를 `participants/<이름>`으로 복사한 후 시작하세요.

---

## Week 1 - 요구사항/용어/컨텍스트

### 목표
기본 요구사항을 분석하고 DDD 관점에서 용어와 컨텍스트를 정의한다.

### TODO 체크리스트

#### 준비 (step0_prepare)
- [ ] `participants/sample` 폴더를 `participants/<내이름>`으로 복사
- [ ] `step0_prepare/progress_template.md` → `progress.md`로 이름 변경
- [ ] `step0_prepare/weekly_progress_template.md` → `weekly_progress.md`로 이름 변경

#### 요구사항 카드 (step1_requirements/requirement_card.md)
- [ ] **목적** 작성: 사용자/비즈니스 문제로 표현
- [ ] **주체** 정의: 고객, 호스트, 시스템 등
- [ ] **행동** 기술: 예약 요청, 승인, 취소 등
- [ ] **규칙** 명시: 불변조건과 제약
- [ ] **상태 변화** 정의: 요청됨 → 확정됨 → 체크인 → 체크아웃
- [ ] **실패 조건** 나열
- [ ] **열린 질문** 최소 2개 작성

#### DDD 분석 (step1_requirements/ddd_analysis.md)
- [ ] Bounded Context 후보 2개 이상 도출
- [ ] Aggregate 후보 정의
- [ ] Domain Event 후보 나열
- [ ] **엔티티** 정의 (Booking, StayProgress 등)
- [ ] **Value Object** 정의 (날짜 범위, 인원 수 등)
- [ ] 규칙을 **정책 vs 불변조건**으로 분류
- [ ] 트랜잭션 경계 설명
- [ ] 선택한 방식 vs 버린 대안 비교

#### 마무리
- [ ] AI와 질문 루프 진행 (최소 3회)
- [ ] `weekly_progress.md` Week1 섹션에 요약 기록
- [ ] 열린 질문/토론거리 메모

### 산출물
- `step1_requirements/requirement_card.md` (완성)
- `step1_requirements/ddd_analysis.md` (초안)
- `step0_prepare/weekly_progress.md` Week1 섹션

---

## Week 2 - 분석/시나리오/테스트

### 목표
Week1의 설계를 시나리오와 테스트로 검증하고, 의도적으로 깨뜨려 보완한다.

### TODO 체크리스트

#### 설계 심화 (step1_requirements/ddd_analysis.md 업데이트)
- [ ] Aggregate 책임 상세화
- [ ] Domain Event 목록 확정
- [ ] 불변조건 보호 방법 명시
- [ ] 트레이드오프 섹션 업데이트

#### 시나리오 작성 (step2_validation/scenario.md)
- [ ] **정상 시나리오** 1개 이상 (Given/When/Then)
- [ ] **예외 시나리오** 1개 이상
- [ ] 상태 변화가 명확히 드러나는지 확인
- [ ] 시나리오로 Week1 설계의 약점 검증

#### 테스트 설계 (step2_validation/test.md)
- [ ] **정상 케이스** 최소 1개
- [ ] **예외 케이스** 최소 1개
- [ ] **경계 케이스** 최소 1개 (체크인 D-Day 등)
- [ ] **위험 케이스** 최소 1개 (순서 오류 등)
- [ ] 시나리오와 테스트 연결 확인

#### 코드 스케치 (선택)
- [ ] 엔티티/VO 클래스 스케치 (Kotlin/Java/TS 중 택1)
- [ ] 핵심 로직만 간단히 작성 (완전한 구현 불필요)

#### 마무리
- [ ] AI와 질문 루프 진행 (설계 약점 찾기)
- [ ] `weekly_progress.md` Week2 섹션에 요약 기록
- [ ] 부족한 영역/TODO 표시

### 산출물
- `step1_requirements/ddd_analysis.md` (심화)
- `step2_validation/scenario.md` (완성)
- `step2_validation/test.md` (완성)
- `step0_prepare/weekly_progress.md` Week2 섹션

---

## Week 3 - 질문 루프 통합/트레이드오프/총정리

### 목표
Week1~2의 질문/수정 내역을 통합하고, 최종 설계를 설명할 수 있는 상태로 정리한다.

### TODO 체크리스트

#### 질문 루프 통합 (step3_integration/question_log.md)
- [ ] 질문 5개 이상 기록
- [ ] 각 질문에 대한 응답/판단 작성
- [ ] 설계에 영향을 준 질문 표시
- [ ] 설계 변경 내용 명시

#### 요약 노트 작성 (step3_integration/summary_outline.md)
- [ ] 요구사항 요약
- [ ] 설계 핵심 설명 (컨텍스트/애그리게잇/이벤트)
- [ ] **핵심 결정 2개** 정리
- [ ] 버린 대안 기록
- [ ] 시나리오/테스트 결과 요약
- [ ] AI 질문으로 수정된 내용 정리
- [ ] 고민 포인트 명시

#### 트레이드오프 정리
- [ ] `ddd_analysis.md` 트레이드오프 섹션 최종 업데이트
- [ ] 일관성 vs 확장성 등 결정 근거 명시

#### 파괴적 요구사항 제안
- [ ] 현 설계를 깨뜨릴 요구사항 1개 이상 제안
- [ ] "왜 현 설계를 깨는가" 설명
- [ ] "어디가 먼저 깨지는가" 명시

#### 마무리
- [ ] `weekly_progress.md` Week3 섹션에 최종 요약
- [ ] 토론 준비 완료

### 산출물
- `step3_integration/question_log.md` (완성)
- `step3_integration/summary_outline.md` (완성)
- `step1_requirements/ddd_analysis.md` (최종)
- `step0_prepare/weekly_progress.md` 전체 완료

---

## 코드 작성 가이드라인

코드는 **선택 사항**이지만, 작성 시 다음을 따르세요:

### 클래스 위주 설계
- 완전한 구현보다 **설계 의도를 보여주는 클래스 스케치**
- 엔티티, Value Object, Aggregate Root 구조에 집중
- 불변조건을 코드로 표현

### 언어 선택 (택 1)
- **Kotlin**: 권장 - 간결하고 불변성 표현이 쉬움
- **Java**: 전통적 DDD 구현에 익숙한 경우
- **TypeScript**: 프론트엔드 경험자

### 예시 위치
- `participants/examples/` 폴더에 언어별 예시 참고
- 코드 파일은 각 step 폴더에 `code/` 서브폴더로 관리

---

## 파일 구조 요약

```
participants/<이름>/
├── step0_prepare/
│   ├── progress.md              # 전체 진행 상태
│   └── weekly_progress.md       # 주차별 상세 로그
├── step1_requirements/
│   ├── requirement_card.md      # 요구사항 카드
│   └── ddd_analysis.md          # DDD 분석
├── step2_validation/
│   ├── scenario.md              # 시나리오 (Given/When/Then)
│   └── test.md                  # 테스트 케이스
└── step3_integration/
    ├── question_log.md          # 질문 루프 로그
    └── summary_outline.md       # 최종 요약 노트
```
