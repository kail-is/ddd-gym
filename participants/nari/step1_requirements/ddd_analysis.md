[STEP 1: DDD 해석]

# DDD 분석 템플릿
요구사항을 해석해 설계 후보를 만들고, 선택/트레이드오프를 명시합니다.

## 체크리스트
- [x] 컨텍스트/애그리게잇/이벤트 후보가 각각 1개 이상이다.
- [x] 규칙을 정책 vs 불변조건으로 분류했다.
- [x] 트랜잭션 경계를 설명했다.
- [x] 선택한 방식과 버린 대안을 비교했다.
- [x] 트레이드오프를 명시했다.

## 문서 구조
1. **Bounded Context 후보**
   1) 예약 컨텍스트 (Booking Context)
   2) 숙소 컨텍스트 (RoomAvailability Context)
   3) 체크인 관리 컨텍스트 (CheckIn Context)

2. **Aggregate 후보**
- 예약 (Reservation)<br>
  `REQUESTED` `CONFIRMED` `CANCELLED` `COMPLETED`
    - 상태 전이 관리
    - 취소 규칙
- 숙소 (RoomAvailability)<br>
  `AVAILABLE` `RESERVED`
    - 예약 가능 여부
    - 객실 예약 상태 관리
- 체크인 (CheckIn)<br>
  `READY` `CHECKED_IN` `CHECKED_OUT`
    - 체크인/체크아웃 관리
- 결제 (Payment)<br>
  `PENDING` `AUTHORIZED` `FAILED` `EXPIRED`
    - 예약 확정 전 결제 진행 상태 관리
    - `HOLD` 만료/연장 시점과 결제 가능 여부 판단
    - 결제 실패 또는 만료가 예약 상태에 어떤 영향을 주는지 연결
3. **Domain Event 후보**<br>
1) Booking Context 이벤트<br>
   - BookingConfirmed
      예약이 최종 확정되었음을 알리고 숙소 점유 후속 처리가 이어져야 할 때
   - BookingCancelled
      예약이 취소되었음을 알리고 점유 해제 또는 후속 정리가 이어져야 할 때
2) RoomAvailability Context 이벤트
   - RoomReserved
      동일 숙소/동일 기간 점유가 완료되었음을 기록해야 예약 확정 결과와 연결될 때
3) CheckIn Context 이벤트
   - CheckOutRecorded
      실제 체크아웃이 기록되어 예약 종료 흐름과 정산/후속 처리가 이어져야 할 때
4) Payment Context 이벤트
   - HoldExtended
      결제 진입 중 `HOLD` 연장이 일어나 예약이 아직 유효하게 진행 중임을 다른 흐름이 알아야 할 때
4. **엔티티 / Value Object 후보**
    - 엔티티
   1) Booking - 예약 계약 자체를 표현하는 엔티티
   2) RoomAvailability -숙소의 예약 가능 상태와 점유 상태를 표현하는 엔티티
   3) CheckIn - 투숙 상태 표현 엔티티
    - VO
    1) DateRange(숙박 기간), GuestCount(숙박 인원), BookingStatus(예약 상태 enum), CheckInStatus(체크인 상태 enum), AvailabilityStatus(숙소 상태 enum)
5. **규칙 분류**
   - 불변조건:
     1) 취소된 예약은 다시 확정될 수 없다
     2) 체크인 이후에는 예약을 취소할 수 없다
     3) 체크인 없이 체크아웃할 수 없다
     4) 확정된 예약은 동일 숙소 동일 기간에 중복으로 점유할 수 없다
   - 정책: ?
6. **트랜잭션 경계**
   - Booking Aggregate의 상태 전이는 하나의 트랜잭션으로 처리한다.
   - RoomAvailability와 Payment는 별도 트랜잭션으로 처리한다.
   - 확정 처리 중 실패가 발생하면 Application Service/Saga가 보상 책임을 가진다.
   - RoomReserved 후 BookingConfirmed 실패 시 RoomReleased를 발행하고, 결제가 성공한 경우 Payment 취소/환불을 요청한 뒤, Booking은 `CANCELLED(reason=CONFIRMATION_FAILED)`로 종료한다.
7. **선택한 방식 vs 버린 대안**
   - 선택: 예약과 체크인을 분리
   - 버린 대안: CheckIn의 상태값(CHECKED_IN, CHECKED_OUT)을 Reservation이 갖고 있음
8. **트레이드오프**
   - 확장성, 책임 분리 명확 / 복잡도 증가, 동기화의 문제

> TIP: AI에게는 "정답을 말하지 말고, 이 설계의 경계/책임 약점을 질문으로 드러내달라"고 요청하세요.
