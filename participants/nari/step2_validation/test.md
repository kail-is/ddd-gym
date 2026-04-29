[STEP 2: 테스트 설계]

# 테스트 템플릿
정상/예외/경계/위험 케이스를 모두 작성해야 다음 단계로 이동할 수 있습니다.

## 체크리스트
- [x] 정상/예외/경계/위험 케이스를 각각 최소 1개 작성했다.
- [x] 시나리오와 테스트가 연결된다.
- [x] 기대 결과와 실패 조건을 명시했다.

## 포맷
| 분류 | 케이스명 | 조건 | 기대 결과 | 참고 시나리오 |
| --- | --- | --- | --- | --- |
| 정상 | 점유 성공 시 예약 확정된다 | 동일 숙소와 동일 기간에 대해 `RoomAvailability`가 점유 가능한 상태 | `Booking.status == CONFIRMED`, `RoomReserved` 이벤트 발생 | 점유 성공 시 예약 확정 |
| 예외 | 중복 예약 확정 시 Booking이 취소된다 | 동일 숙소와 겹치는 기간에 이미 점유된 상태가 있고, 새 Booking은 `REQUESTED` 상태 | `Booking.status == CANCELED`, `BookingCanceled.reason == AVAILABILITY_FAILED` | 중복 점유로 실패 시 예약 취소 |
| 경계 | HOLD 만료 직후 확정 시 Booking이 취소된다 | Booking과 연결된 `HOLD`가 방금 만료된 상태 | `Booking.status == CANCELED` | HOLD 만료 직후 예약 확정 실패 |
| 위험 | HOLD 만료 시점 결제 진입 시 HOLD를 연장한다 | Booking과 연결된 `HOLD`의 만료 시점이 임박한 상태에서 사용자가 결제 요청을 시작함 | `HOLD`가 연장되어 결제를 진행할 수 있는 상태가 됨 | HOLD 만료 시점 결제 진입 |
| 위험 | RoomReserved 후 BookingConfirmed 실패 시 보상 흐름이 시작된다 | `Booking`은 `REQUESTED` 상태이고, `Payment`는 성공했으며, `RoomReserved`는 성공한 상태에서 `Booking`을 `CONFIRMED`로 전이하는 단계가 실패함 | 보상 흐름이 시작되고 `RoomReleased`가 발생하며 결제 취소/환불 요청이 발생한다. `Booking`은 `CONFIRMED`가 되지 않고 최종적으로 `BookingCancelled(reason=CONFIRMATION_FAILED)`가 기록된다 | 확정 처리 중 내부 상태 전이 실패 |

## 예시
| 분류 | 케이스명 | 조건 | 기대 결과 | 참고 시나리오 |
| --- | --- | --- | --- | --- |
| 정상 | 체크인 전 확정 | 숙소 재고 존재, 호스트 정산 OK | 상태 `확정됨`, 이벤트 `BookingConfirmed` | 체크인 이전 확정 – 정상 |
| 예외 | 중복 날짜 거절 | 동일 숙소/날짜 `확정됨` 존재 | 상태 `거절`, 사유 기록 | 중복 날짜 거절 – 예외 |
| 경계 | 체크인 D-Day 취소 | 체크인 2시간 전 취소 요청 | 취소 거절, 사유 "체크인 이후" | 체크인 임박 취소 시나리오 |
| 위험 | 체크아웃 순서 오류 | `체크인` 상태 없이 체크아웃 요청 | 트랜잭션 롤백, 알림 기록 | 순서 검증 시나리오 |
