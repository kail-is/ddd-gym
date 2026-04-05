[STEP 1: AI 티키타카 워크플로우]

# ChatGPT/Claude 대화형 진행법
이 문서는 ChatGPT나 Claude와 직접 대화하며 스터디를 진행할 때 챗봇이 지켜야 할 공통 규칙을 정리한 것입니다. 상세 주차 흐름은 `ai_playbooks/STEP1_chat_workflow.md`에서 확인합니다.

## 1. 세션 시작
1. `STEP0_AI_START_PROMPT.md`를 붙여넣으면 기본 설정이 끝납니다.
2. 챗봇은 사용자의 이름/핸들을 확인하고, 이번 세션에서 복습(개념/이전 산출물 점검)이 필요한지 먼저 묻습니다.
3. 복습이 필요하면 `study_guides/how_to_study.md`, `shared_topics/stepX_*.md`, 최근 산출물을 짧게 요약해 되짚습니다.
4. `participants/<이름>/step0_prepare/weekly_progress.md`의 최신 상태를 확인하고 요약합니다.
5. 주차를 선택하면 `README.md`와 `shared_topics/stepX_*.md`를 요약해 해당 주차 목표/제약을 상기시킵니다.

## 2. 질문-답변 루프
- 챗봇은 `01_templates`를 참조하여, "지금 답변을 `participants/<이름>/weekX/<파일>.md`의 `<섹션>`에 붙여넣으세요"라고 안내합니다.
- 사용자는 답변을 해당 Markdown 파일에 적고, 완료 시 "붙여넣음" 또는 "done"이라고 응답합니다.
- 챗봇은 필요한 경우 `participants/examples/stepX_*/..._example.md` 파일에 있는 예시를 간략히 설명해 힌트를 제공합니다.
- 각 섹션이 완료되면 챗봇은 `participants/<이름>/step0_prepare/progress.md`의 해당 체크박스를 ☑️ 처리하고 링크를 기록하도록 요청합니다.

## 3. 단계 전환 규칙
- 단계별 질문 목록은 `STEP1_chat_workflow.md`에 있으므로 그 순서를 그대로 지킵니다.
- Week1(요구사항/용어/컨텍스트)을 마치면 "DDD 해석으로 넘어갈까요?"처럼 다음 단계 의사를 확인합니다.
- Week2에서는 애그리게잇/이벤트 → 시나리오 → 테스트 순으로 나아가며, 매 단계 종료 시 `participants/<이름>/step0_prepare/weekly_progress.md`에 요약 블록을 제안합니다.
- Week3에서는 누적된 질문 루프/트레이드오프/요약 체크리스트를 다루고, 마지막에 TODO/토론 포인트를 정리합니다.

## 4. 기록 방법
- 챗봇이 제공하는 요약 블록을 `participants/<이름>/step0_prepare/progress.md`와 `participants/<이름>/step0_prepare/weekly_progress.md`에 복사합니다.
- 각 질문에 답변을 붙여넣었으면 반드시 챗봇에게 알리고, 챗봇은 다음 질문으로 넘어가도 좋은지 확인합니다.
- 주차가 끝나면 챗봇이 진행 로그/다음 액션을 정리해 주고, 사용자는 해당 내용을 그대로 문서에 기록합니다.

## 5. 대화 종료
- Week3까지 완료했거나 중단하려면 "세션 종료"라고 말합니다.
- 챗봇은 마지막으로 다음을 bullet로 정리합니다:
  1. 이번 세션에서 작성/수정한 파일과 섹션
  2. 미완료 항목/TODO
  3. 추천 요약/토론 액션(필요 시)

## 체크리스트
- [ ] 챗봇이 이름/핸들 및 이전 진행 상황을 확인한다.
- [ ] 각 질문은 템플릿 경로와 붙여넣을 섹션을 명시한다.
- [ ] 단계가 끝날 때마다 `participants/<이름>/step0_prepare/progress.md`와 `participants/<이름>/step0_prepare/weekly_progress.md` 업데이트를 검증한다.
- [ ] 세션 종료 시 작성 파일/미완료 작업/다음 액션을 요약한다.
