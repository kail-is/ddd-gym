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
- [x] Week1 기록 완료
- [x] Week2 기록 완료
- [x] Week3 기록 완료

---

### Step 1 – Week1
- **참가자**: 김나리
- **진행 내용 요약**: 컨텍스트/애그리게이트 정의, 모델링 
- **AI 대화 로그 링크/메모**: https://www.notion.so/343cf724201480fb9a8ce86e7f10a4e5?source=copy_link
- **PR/노션 링크**: 
- **다음 액션**: 

### Step 2 – Week2
- **참가자**: 김나리
- **진행 내용 요약**: 예약 확정/실패 흐름을 기준으로 Given/When/Then 시나리오 4개(정상/예외/경계/위험) 작성, 테스트 케이스와 매핑 완료
- **AI 대화 로그 링크/메모**: https://www.notion.so/343cf724201480fb9a8ce86e7f10a4e5?source=copy_link / `RoomAvailability` 점유 가능 여부, 중복 점유 실패 시 `BookingCanceled(reason=AVAILABILITY_FAILED)` 처리, `HOLD` 만료/연장 경계 상황을 기준으로 설계 검증
- **PR/노션 링크**: 
- **다음 액션**: Week3에서 질문 로그 통합, `HOLD`/예약 확정 책임 경계와 이벤트 의미를 다시 점검하며 트레이드오프 정리

### Step 3 – Week3
- **참가자**: 김나리
- **진행 내용 요약**: 질문 루프를 통해 `RoomReserved`를 예약 확정의 전제 사실로 정리하고, `Booking.status`는 최종 결과만 표현한다는 기준을 명확히 함. 트랜잭션 경계는 Booking 내부 상태 전이와 외부/별도 트랜잭션을 분리하고, 실패 시 Application Service/Saga의 보상 책임까지 문서화함.
- **AI 대화 로그 링크/메모**:  https://www.notion.so/343cf724201480fb9a8ce86e7f10a4e5?source=copy_link / `BookingCancelled`는 여러 종료 이유를 `reason`으로 구분하는 방향을 유지. `REQUESTED`가 다양한 진행 중 의미를 품고 있어 조회 모델이 있으면 좋겠다는 판단을 남김. `RoomReserved` 후 `BookingConfirmed` 실패 시 `RoomReleased`, 결제 취소/환불 요청, `BookingCancelled(reason=CONFIRMATION_FAILED)`로 이어지는 보상 테스트를 추가함.
- **PR/노션 링크**: 
- **다음 액션**: 조회 모델 필요 여부와 `REQUESTED`의 진행 중 의미를 오프라인 토론 거리로 남기고, 최종 공유/PR 정리
