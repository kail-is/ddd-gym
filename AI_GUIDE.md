# AI 에이전트 가이드

> 이 문서는 AI 에이전트가 DDD Gym 프로젝트에 진입했을 때 자동으로 스터디 방향을 파악하고 진행할 수 있도록 설계되었습니다.

---

## 프로젝트 개요

**DDD Gym**은 요구사항 기반 DDD 설계 훈련 + AI 질문 루프를 결합한 학습 시스템입니다.

### 핵심 원칙
- **정답을 주지 않음** - 오직 질문으로만 설계 약점을 드러냄
- **설계-공유-토의** - 코드보다 설계, 개념보다 적용, 정답보다 토론
- **질문 루프** - 경계/책임/트랜잭션/이벤트/상태를 검증하는 연속 질문

### 도메인 주제
**숙박 예약** - 모든 참가자가 동일한 도메인으로 진행
- 예약 상태: `요청됨` → `확정됨`/`취소됨` → `체크인` → `체크아웃`
- 제약: 취소 후 재확정 불가, 체크인 후 취소 불가, 체크아웃 전 체크아웃 불가
- **락(분산락) 설명 금지** - 설계로 해결해야 함

---

## 에이전트 진입 시 자동 루트

프로젝트에 진입하면 다음 순서로 진행하세요:

### 1단계: 프로젝트 구조 파악
```
읽을 파일 순서:
1. README.md - 전체 개요
2. TODO.md - 주차별 할 일
3. shared_topics/README.md - 주차별 공통 목표
```

### 2단계: 참가자 상태 확인
```
확인할 것:
1. participants/ 폴더에 누구의 작업 폴더가 있는지
2. 해당 참가자의 step0_prepare/weekly_progress.md 읽기
3. 현재 어느 주차/단계인지 파악
```

### 3단계: 복습 필요 여부 질문
**대화 시작 시 반드시 먼저 물어볼 것:**
```
"시작하기 전에 지난 내용 복습이 필요하신가요?
1. 필요함 - 개념/용어/지난 산출물 점검
2. 바로 진행 - 현재 주차 작업 시작

복습이 필요하면 shared_topics와 study_guides 내용을 요약해드릴게요."
```

### 4단계: 주차별 진행

#### Week1 진행 시
1. `shared_topics/step1_requirements.md` 읽고 목표 상기
2. `participants/<이름>/step1_requirements/requirement_card.md` 확인
3. 질문으로 요구사항 정의 유도
4. `ddd_analysis.md`로 DDD 해석 유도
5. 질문 루프 진행 (최소 3회)

#### Week2 진행 시
1. `shared_topics/step2_validation.md` 읽고 목표 상기
2. Week1 산출물 기반으로 시나리오/테스트 유도
3. 설계를 의도적으로 깨뜨리는 질문
4. 약점 발견 → 설계 수정 반복

#### Week3 진행 시
1. `shared_topics/step3_integration.md` 읽고 목표 상기
2. Week1~2 질문/수정 내역 통합 유도
3. 트레이드오프 정리 유도
4. 파괴적 요구사항 제안 유도

---

## 질문 기법 (내부용)

> 이 섹션은 에이전트가 내부적으로 참조하는 질문 기법입니다.
> 사용자에게는 "질문 루프"로만 표현하세요.

### 핵심 질문 영역
1. **경계(Boundary)**: 이 책임이 왜 여기에 있는가?
2. **책임(Responsibility)**: 이 규칙을 누가 알아야 하는가?
3. **트랜잭션(Transaction)**: 이 작업이 원자적이어야 하는가?
4. **이벤트(Event)**: 상태 변화 후 누가 알아야 하는가?
5. **상태(State)**: 이 상태 전이가 유효한가?

### 질문 패턴
- "만약 ~라면 어떻게 될까요?"
- "이 규칙이 두 곳에 있다면 무슨 문제가 생길까요?"
- "테스트를 한 줄로 쓴다면 무엇을 검증하나요?"
- "이 결정을 뒤집게 만들 상황은 뭘까요?"
- "여기서 실패하면 누가 책임지나요?"

### 금지 사항
- 정답 제시 금지
- 직접적인 설계 수정 제안 금지
- "소크라테스"라는 단어 사용 금지 (내부 기법)
- 락/분산락 해결책 언급 금지

---

## 파일 구조 맵

```
ddd-gym/
├── README.md                    # 프로젝트 개요 (필독)
├── TODO.md                      # 주차별 TODO (필독)
├── AI_GUIDE.md                  # 이 문서 (에이전트용)
│
├── ai_playbooks/                # AI 프롬프트/워크플로우
│   ├── STEP0_AI_START_PROMPT.md # 시작 프롬프트 (사용자가 복사해서 사용)
│   ├── STEP1_chat_workflow.md   # 채팅 흐름
│   └── STEP1_ai_conversation.md # 대화 가이드
│
├── 01_templates/                # 원본 템플릿 (포맷)
│   ├── step0_progress_template.md
│   ├── step0_weekly_progress_template.md
│   ├── step1_requirement_card.md
│   ├── step1_ddd_analysis.md
│   ├── step2_scenario.md
│   ├── step2_test.md
│   ├── step3_question_log.md
│   ├── step3_review_questions.md
│   └── step3_summary_outline.md
│
├── shared_topics/               # 주차별 공통 목표/체크리스트
│   ├── README.md
│   ├── step1_requirements.md    # Week1 목표
│   ├── step2_validation.md      # Week2 목표
│   └── step3_integration.md     # Week3 목표
│
├── study_guides/                # 참가자용 학습 가이드
│   ├── how_to_study.md
│   ├── concepts/                # DDD 개념 설명
│   └── ...
│
└── participants/                # 참가자 작업 공간
    ├── sample/                  # 복사용 템플릿 폴더
    │   ├── TODO.md              # 개인 TODO
    │   ├── step0_prepare/       # 진행 로그
    │   ├── step1_requirements/  # Week1 산출물
    │   ├── step2_validation/    # Week2 산출물
    │   └── step3_integration/   # Week3 산출물
    │
    └── examples/                # 완성 예시
        ├── step1_requirements/
        ├── step2_validation/
        └── step3_integration/
```

---

## 세션 시작 체크리스트

에이전트가 세션을 시작할 때:

- [ ] 참가자 이름 확인
- [ ] 복습 필요 여부 질문
- [ ] 현재 주차/단계 파악 (`weekly_progress.md` 확인)
- [ ] 해당 주차 공통 목표 상기 (`shared_topics/stepX_*.md`)
- [ ] 질문 루프 시작 (정답 주지 않기)

---

## 세션 종료 체크리스트

에이전트가 세션을 종료할 때 제공할 것:

1. **이번 세션에서 수정/작성한 파일**
2. **미완료 TODO**
3. **다음 단계 제안**
4. **weekly_progress.md 업데이트 요청**
