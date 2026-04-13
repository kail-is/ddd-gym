[STEP 0: 주차별 진행 로그]

# Weekly Progress Log
START 챗봇과 대화를 끝낼 때마다 이 파일을 갱신해 다음 세션 재진입 포인트를 남깁니다.

## 사용 방법
1. START 챗봇이 주차를 묻기 전에 전체 체크리스트로 미완료 주차를 확인합니다.
2. 주차 섹션은 다음 세부 항목을 요약하도록 되어 있습니다.
   - **Week1**: 요구사항 카드, 용어/컨텍스트, 열린 질문, PR 제출.
   - **Week2**: 애그리게잇/이벤트 해석, 시나리오, 테스트, PR 제출.
   - **Week3**: 질문 루프 통합, 트레이드오프, 최종 PR 제출.
3. 각 항목을 채운 뒤 체크박스를 표시하고, PR/노션/파일 링크를 기록해 다음 대화에서 바로 이어갈 수 있게 합니다.

## 전체 체크리스트
- [X] Week1 기록 완료
- [ ] Week2 기록 완료
- [ ] Week3 기록 완료

---

### Step 1 – Week1
- **참가자**: Minsu
- **진행 내용 요약**: 
  - 도메인: 대실(시간대별 숙소 예약) 시스템
  - 요구사항 카드 완성 (목적/주체/행동/규칙/상태변화/실패조건/열린질문)
  - DDD 분석 초안 작성
    - Bounded Context 4개 도출: 예약 관리 / 숙소 관리 / 정산 관리 / 결제 관리
    - Aggregate: Reservation (예약 관리), PricingPolicy (숙소 관리)
    - Domain Service: ReservationOverlapChecker (중복 예약 확인)
    - Domain Event 5개: ReservationConfirmed, CustomerReservationCancelled, HostReservationCancelled, ReservationCheckedIn, ReservationCheckedOut
    - 불변조건 5개 / 정책 2개 분류 완료
    - 트랜잭션 경계: 예약 관리 내 단일 트랜잭션, 정산 관리는 이벤트 기반 처리
    - 트레이드오프: 컨텍스트 분리로 독립성 확보 vs 일관성 처리 복잡도 증가
- **AI 대화 로그 링크/메모**: 
  - 스케줄링 시스템은 예약 관리 컨텍스트 책임으로 흡수 결론
  - 가격 정책은 숙소 관리 컨텍스트에서 관리, 예약 생성 시 스냅샷으로 저장
  - 정산 시스템과의 동기화는 이벤트 기반으로 처리 (별도 DB이므로 단일 트랜잭션 불가)
- **PR/노션 링크**: -
- **다음 액션**: 
  - requirement_card.md 규칙 항목 중복/모순 수정
  - ddd_analysis.md Domain Service 섹션 분리 (ReservationOverlapChecker 이동)
  - Week2: 시나리오(Given/When/Then) 및 테스트 케이스 작성 시작

### Step 2 – Week2
- **참가자**: 
- **진행 내용 요약**: 
- **AI 대화 로그 링크/메모**: 
- **PR/노션 링크**: 
- **다음 액션**: 

### Step 3 – Week3
- **참가자**: 
- **진행 내용 요약**: 
- **AI 대화 로그 링크/메모**: 
- **PR/노션 링크**: 
- **다음 액션**: 
