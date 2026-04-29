[STEP 2: 시나리오 작성]

# 시나리오 템플릿 (Given/When/Then)

## 체크리스트
- [x] 정상 시나리오 1개 이상.
- [x] 예외 시나리오 1개 이상.
- [x] 각 시나리오는 Given/When/Then 구조를 따른다.
- [x] 상태 변화가 명확히 드러난다.

## 포맷
### 점유 성공 시 예약 확정
- **종류**: 정상
- **Given**: 동일 숙소와 동일 기간에 대해 `RoomAvailability`가 점유 가능한 상태이다
- **When**: 사용자가 해당 숙소와 기간으로 예약 확정을 시도한다
- **Then**: Booking은 `CONFIRMED`로 전이되고, `RoomReserved` 이벤트가 발생한다

### 중복 점유로 실패 시 예약 취소
- **종류**: 예외
- **Given**: 동일 숙소와 겹치는 기간에 대해 이미 점유된 상태가 있고, 새 Booking은 `REQUESTED` 상태이다
- **When**: 사용자가 해당 숙소와 기간으로 예약 확정을 시도한다
- **Then**: Booking은 `CONFIRMED` 되지 않고 `CANCELED`로 전이되며, `BookingCanceled(reason=AVAILABILITY_FAILED)` 이벤트가 발생한다

### HOLD 만료 직후 예약 확정 실패
- **종류**: 경계
- **Given**: Booking과 연결된 `HOLD`가 방금 만료되었다
- **When**: 사용자가 해당 Booking의 예약 확정을 시도한다
- **Then**: Booking은 `CONFIRMED` 되지 않고 `CANCELED`로 전이된다

### HOLD 만료 시점 결제 진입
- **종류**: 위험
- **Given**: Booking과 연결된 `HOLD`의 만료 시점이 임박했다
- **When**: 사용자가 결제 요청을 시작한다
- **Then**: `HOLD`가 연장되어 결제를 진행할 수 있는 상태가 된다

## 예시
### 체크인 이전 확정 – 정상
- **종류**: 정상
- **Given**: 숙소 A는 7/20~7/22 재고가 남아 있고 호스트는 정산 보류 상태가 아니다.
- **When**: 고객 B가 위 날짜로 예약을 요청한다.
- **Then**: 예약 상태가 `확정됨`이 되고 `BookingConfirmed` 이벤트가 발행된다.

### 중복 날짜 거절 – 예외
- **종류**: 예외
- **Given**: 숙소 A는 7/20~7/22 예약이 이미 `확정됨` 상태다.
- **When**: 다른 고객이 동일 날짜를 요청한다.
- **Then**: 예약이 `거절됨` 처리되고 사유 "중복 날짜"가 기록된다.
