# 내 TODO - DDD Gym

> 이 폴더를 복사한 후 아래 체크리스트를 따라 진행하세요.
> 전체 가이드는 루트의 `TODO.md`를 참고하세요.

---

## 시작 전 준비
- [x] 이 폴더(`sample`)를 `participants/<내이름>`으로 복사
- [x] `step0_prepare/progress_template.md` → `progress.md`로 이름 변경
- [x] `step0_prepare/weekly_progress_template.md` → `weekly_progress.md`로 이름 변경

---

## Week 1 체크리스트

### 요구사항 카드 (`step1_requirements/requirement_card.md`)
- [x] 목적 작성
- [x] 주체/행동/규칙 정의
- [x] 상태 변화/실패 조건 기술
- [x] 열린 질문 2개 이상

### DDD 분석 (`step1_requirements/ddd_analysis.md`)
- [x] Bounded Context 2개 이상
- [x] Aggregate/Event 후보
- [x] 엔티티/VO 정의
- [x] 정책 vs 불변조건 분류

### 마무리
- [x] AI 질문 루프 3회 이상
- [x] `weekly_progress.md` Week1 기록

---

## Week 2 체크리스트

### 시나리오 (`step2_validation/scenario.md`)
- [x] 정상 시나리오 1개 이상
- [x] 예외 시나리오 1개 이상

정상:
  - 예약요청 → 결제 → 확정
  - 체크인 → 체크아웃
  - 취소요청 → 호스트 확정 → 환불

예외:
  - 이선좌 잠금 중 다른 사용자 예약 시도 → 거절
  - 결제 실패 후 재시도 성공
  - 폴링 끊김 후 잠금 해제
  - 취소된 예약 재확정 시도 → 거절
  - 체크인 없이 체크아웃 시도 → 거절
  - 취소 요청 대기 중 체크인 시도 → 거절
  - 호스트 취소 거절 → Booking 영향 없음

(학습용) 부분 취소:
  - 1박 부분 취소
  - 전체 날짜 부분 취소 → Booking 취소

### 테스트 (`step2_validation/test.md`)
- [x] 정상/예외/경계/위험 각 1개 이상

정상:
  - 예약 → 확정, 체크인 → 체크아웃, 취소요청 → 확정 → 환불
  - 잠금 중 재시도 성공, 취소 요청 생성, 호스트 확정/거절

예외:
  - 취소된 예약 재확정, 체크인 없이 체크아웃, 체크인 후 취소
  - 잠금 중 타 사용자 예약, 취소 대기 중 체크인

경계:
  - 폴링 끊김 9분59초(유지), 10분(해제)
  - 환불금액 = 결제금액, 취소 거절 직후 체크인

위험:
  - 잠금 해제 직전 결제 성공 경합
  - 잠금 해제와 결제 동시
  - 취소 확정과 체크인 동시

### 마무리
- [x] AI 질문 루프 (설계 약점 찾기)
- [x] `weekly_progress.md` Week2 기록

---

## Week 3 체크리스트

### 질문 로그 (`step3_integration/question_log.md`)
- [ ] 질문 5개 이상 정리
- [ ] 설계 변경 여부 표시

### 요약 노트 (`step3_integration/summary_outline.md`)
- [ ] 핵심 결정 2개
- [ ] 버린 대안
- [ ] AI 질문으로 수정된 내용

### 마무리
- [ ] 파괴적 요구사항 1개 제안
- [ ] `weekly_progress.md` Week3 기록
- [ ] 토론 준비 완료

---

## 코드 작성 (선택)
- [x] 엔티티/VO 클래스 스케치 (Kotlin/Java/TS 택1)
- [x] `code/` 폴더에 저장
