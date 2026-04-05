[STEP 1: 채팅 워크플로우]

# START 버튼 이후 챗봇 흐름

이 저장소는 \"START\" 프롬프트(또는 Issue/Discussion Bot)를 실행하면 AI가 질문만 던지며 주차별 산출물을 안내하도록 설계되었습니다. 아래 흐름을 봇 프롬프트에 그대로 활용하세요. 참가자는 답변과 \"파일 업데이트 완료\" 같은 확인만 하면 되며, 템플릿 복사/폴더 생성/체크박스 토글 같은 반복 작업은 모두 AI 지시에 따라 자동 처리됩니다.

## 0. 주차 선택 프롬프트
```
정답 금지, 질문만 생성. 사용자가 지금 어느 주차를 진행할지 먼저 묻고, 아래 규칙에 따라 단계별 질문을 순서대로 던져라.
- Week1: 요구사항 → 용어 → 컨텍스트
- Week2: 애그리게잇 → 이벤트 → 시나리오 → 테스트
- Week3: 질문 루프 통합 → 트레이드오프 → 요약 정리
```

### 대화 예시
```
AI: 이번에 진행할 단계는 어느 주차인가요? (week1/week2/week3)
사람: week1
AI: 좋습니다. 요구사항 카드를 작성하기 위해 목적부터 질문할게요. 현재 고객/호스트의 목적은 무엇인가요?
```

## 1. Week1 채팅 플로우
1. **요구사항 카드**
   - AI 질문 예: \"목적을 한 문장으로 정의하면?\", \"주체/행동/규칙을 구분해 적을 수 있나요?\"
   - AI가 `01_templates/step1_requirement_card.md`를 `participants/<이름>/step1_requirements/`에 복사하고, 참가자는 답변만 제공하며 반영 여부를 확인한다.
2. **용어/컨텍스트**
   - AI 질문 예: \"숙박 예약 상태를 나타내는 용어 5가지는?\", \"컨텍스트를 나누는 기준은?\"
   - 사용자는 `shared_topics/step1_requirements.md` 체크리스트를 참고해 답한다.
3. **Week1 종료**
   - AI가 \"Week1 체크리스트가 모두 끝났나요?\" 확인 → 완료 시 `participants/<이름>/step0_prepare/weekly_progress.md`에 Week1 섹션을 추가한다.

## 2. Week2 채팅 플로우
1. **애그리게잇/이벤트**
   - AI 질문 예: \"Booking 루트가 보호해야 할 불변조건은?\", \"어떤 이벤트가 상태를 연결하나요?\"
2. **시나리오/테스트 작성**
   - AI 질문 예: \"Given/When/Then 정상 시나리오는?\", \"테스트에서 경계 케이스로 뭘 다루나요?\"
   - AI가 `01_templates/step2_scenario_template.md`, `test_template.md`를 복사/갱신하고 참가자가 답변한다.
3. **Week2 종료**
   - AI가 \"변경된 부분을 `participants/<이름>/step0_prepare/weekly_progress.md` Week2에 요약했나요?\" 라고 확인한다.

## 3. Week3 채팅 플로우
1. **질문 루프 정리**
   - AI 질문 예: \"경계 약점을 드러내는 질문 5가지를 더 만들까요?\"
2. **트레이드오프/요약 정리**
   - AI 질문 예: \"어떤 질문이 설계를 뒤집었나요?\", \"PR 템플릿 \"AI 질문으로 수정된 내용\" 섹션에 넣을 핵심은?\"
3. **Week3 종료**
   - 사용자는 `participants/<이름>/step0_prepare/weekly_progress.md` Week3 항목을 채우고, 봇은 공유 노트를 정리했는지 확인한 뒤 대화를 종료한다.

## 4. 주차 종료 후 파일 반영 규칙
- 대화가 끝나면 AI가 `participants/<이름>/stepX_*` 폴더에 수정본을 저장하고, 참가자는 정상 반영됐는지 확인한다.
- 동시에 AI가 `participants/<이름>/step0_prepare/progress.md`와 `participants/<이름>/step0_prepare/weekly_progress.md`에 해당 주차 섹션을 추가/업데이트하므로, 참가자는 요약 내용이 맞는지만 검토한다.

## 체크리스트
- [ ] START 버튼을 눌렀을 때 봇이 주차부터 묻는다.
- [ ] 각 단계에서 템플릿 파일 경로를 안내한다.
- [ ] Week 종료 시 `participants/<이름>/step0_prepare/weekly_progress.md`가 갱신된다.
- [ ] 봇은 끝까지 정답 대신 질문으로만 이끈다.
