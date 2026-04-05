# DDD 스터디 학습 시스템

> 이 저장소는 요구사항 기반 DDD 설계 훈련과 AI 질문 루프를 결합한 학습 시스템입니다.

## 이번 스터디 주제: 숙박 예약
- **모두 동일한 숙박 예약 도메인을 설계**하고 주차별 결과를 요약 노트로 공유한 뒤 토론합니다. 목표는 사고 방식을 깨는 것이지 완벽한 정답이 아닙니다.
- **핵심 워크플로우**: 설계 → 공유 → 토의. 구현은 선택 사항이며 설계 근거/비교가 중심입니다.
- **기본 요구사항**
  - 고객은 숙소를 예약할 수 있다.
  - 예약 상태: `요청됨` → `확정됨`/`취소됨` → `체크인` → `체크아웃`.
  - 제약: 취소된 예약은 다시 확정 불가, 체크인 이후 취소 불가, 체크아웃 전 체크아웃 불가.
  - **락(분산락 등) 설명은 금지**. 설계 자체에서 다룰 수 있는 책임/경계를 우선한다.
- **설계 질문 힌트**
  - 취소 사유/상태별 데이터는 어디서 책임지나?
  - 체크인 가능 여부는 어떤 컨텍스트가 판단하나?
  - 재확정 시도를 어디서 차단하나?
  - 규칙을 가장 잘 아는 곳은 어디이고, 중복되면 어떤 리스크가 있는가?
  - 테스트 한 줄로 쓴다면 어떤 모듈을 호출해야 하는가?
- **평가 기준**
  - 규칙이 어디에 모여 있는가?
  - 요구사항 추가 시 어디가 먼저 깨지는가?
- 세부 진행 흐름과 질문 스크립트는 `ai_playbooks/STEP1_ai_conversation.md`, `ai_playbooks/STEP1_chat_workflow.md`에 정리되어 있으며 AI START 프롬프트가 그대로 따릅니다.

> **AI START**  
> 1. `ai_playbooks/STEP0_AI_START_PROMPT.md` 파일을 열어 전체 프롬프트를 복사합니다.  
> 2. ChatGPT/Claude에 붙여넣고 “Week1부터 진행하자”처럼 원하는 주차를 알려주세요.  
> 3. AI 코치가 이름과 진행 상태를 묻고, README/템플릿/`participants/<이름>/step0_prepare/weekly_progress.md`를 기반으로 워크플로우를 안내합니다.

> **기억할 것**  
> - 문제를 어떻게 해석했는지, 어떤 질문으로 설계를 바꿨는지에 집중합니다.  
> - 막히면 "이건 토론거리로 남길게"라고 적어두고 다음 질문으로 넘어가도 됩니다.  
> - 잠시 쉬었다 올 땐 `participants/<이름>/step0_prepare/weekly_progress.md`만 다시 확인하고 챗봇에게 어디까지 했는지 알려주면 됩니다.
> - 각 주차 작업이 끝나면 간단한 요약 노트를 정리하고, 스터디 시간에는 그 노트를 참고해 구두로 토론합니다.

## 빠른 링크
- `TODO.md`: **주차별 명확한 할 일 목록** (필독)
- `AI_GUIDE.md`: AI 에이전트 자동 진입 가이드
- `ai_playbooks/STEP0_AI_START_PROMPT.md`: 프롬프트 복사 후 바로 실행
- `shared_topics/step1_requirements.md`: Week1 공통 체크리스트
- `participants/sample/`: **복사해서 바로 시작할 수 있는 작업 폴더**
- `participants/examples/code/`: **Kotlin/Java/TypeScript 코드 예시**

## 폴더 맵
```
ddd-gym/
├── TODO.md                 # 주차별 명확한 할 일 목록 (필독)
├── AI_GUIDE.md             # AI 에이전트 자동 진입 가이드
├── ai_playbooks/           # AI 프롬프트와 질문 시나리오
├── study_guides/           # 참가자용 학습/개념 가이드
├── 01_templates/           # 원본 템플릿 (포맷)
├── shared_topics/          # 주차별 공통 체크리스트
└── participants/
    ├── sample/             # 복사해서 바로 시작 (템플릿 내용 포함)
    │   ├── TODO.md         # 개인 TODO 체크리스트
    │   ├── step0_prepare/  # 진행 로그
    │   ├── step1_requirements/  # Week1 산출물
    │   ├── step2_validation/    # Week2 산출물
    │   └── step3_integration/   # Week3 산출물
    └── examples/           # 완성 예시
        └── code/           # Kotlin/Java/TS 코드 예시
```

## 필수 참고 문서
- `ai_playbooks/STEP0_AI_START_PROMPT.md`: 챗봇에게 줄 전체 지시문.
- `ai_playbooks/STEP1_chat_workflow.md`: AI가 어떤 순서/명령으로 템플릿을 채우게 할지 정의.
- `study_guides/how_to_study.md`: 재진입과 최소 제출 기준.
- `participants/examples/`: 답변이 어떤 형태인지 감 잡는 예시.
- `participants/sample/`: AI가 복사해 쓸 수 있는 기본 폴더/README 구조.
- `shared_topics/stepX_*.md`: 각 주차 공통 목표/제약/토의 거리 안내.

## 운영 원칙
- 코드보다 설계, AI보다 판단, 정답보다 토론, 개념보다 적용.
- 각 단계 문서는 독립 실행 가능해야 하며 상단의 STEP 표시와 체크리스트를 반드시 유지합니다.
- AI는 답을 내지 않고 질문을 던지게 하며, 설계 약점을 드러내는 용도로만 사용합니다.
- 모든 산출물은 시나리오와 테스트 문서를 통해 검증합니다.

## 세션 Todo (AI가 주차별로 안내)
> 각 단계마다 `participants/<이름>/step0_prepare/weekly_progress.md`에 질문 루프 로그를 남겨 설계 약점을 찾습니다. Week3는 그동안 쌓인 질문/수정 내역을 주간 요약으로 정리하는 단계입니다. 리뷰/검토는 스터디 시간에 구두로 진행합니다.

### Week1 – 요구사항/용어/컨텍스트
- 간단한 상태에서 출발해 요구사항 카드와 용어/컨텍스트 메모를 작성하고, 질문 루프로 규칙/경계를 검증하며 열린 질문을 `participants/<이름>/step0_prepare/weekly_progress.md`의 Week1 섹션에 남깁니다. 이 단계에서 설계의 첫 기반을 마련합니다.

### Week2 – 분석/시나리오/테스트
- Week1에서 만든 설계를 시나리오/테스트로 깨보며 DDD 해석, 시나리오, 테스트 템플릿을 채웁니다. 매 단계마다 질문 루프로 새로운 요구사항·제약을 점검하고, 부족한 영역/TODO를 `participants/<이름>/step0_prepare/weekly_progress.md` Week2 섹션에 남깁니다.

### Week3 – 질문 루프 통합/트레이드오프/주간 정리
- 앞선 두 주 동안 쌓인 질문/수정 내역을 통합해 더 복잡해진 설계를 설명하고 트레이드오프를 명시합니다. Week3 요약 노트는 토론 포인트를 `participants/<이름>/step0_prepare/weekly_progress.md` Week3 섹션에 정리해 설계 근거를 공유합니다.

각 Todo는 챗봇이 "다음은 시나리오 템플릿을 채워보자" 식으로 지시하므로, 참가자는 답변과 검토만 하면 됩니다.
