[STEP 2: 시나리오 작성]

# 시나리오 템플릿 (Given/When/Then)

## 체크리스트
- [x] 정상 시나리오 1개 이상.
- [x] 예외 시나리오 1개 이상.
- [x] 각 시나리오는 Given/When/Then 구조를 따른다.
- [x] 상태 변화가 명확히 드러난다.

---

### 시나리오 1: 예약 요청 → 결제 완료 → 확정됨 – 정상
- **종류**: 정상
- **Given**: 숙소 A의 7/20~7/22 일정에 Redis 선점 키가 없고, 해당 일정이 예약 가능 상태다.
- **When**:
  1. 고객 B가 예약 요청 → 숙소 컨텍스트가 Redis 키(`숙소ID + DateRange`, Value: `BookingID`) 생성
  2. 결제 컨텍스트가 결제 처리 완료 → `TransactionCompleted` 발행 (Kafka key: BookingID)
  3. 숙소 컨텍스트가 Redis 키 확인 → 선점 유효 → 일정 확정, Redis 키 삭제 → `BookingConfirmed` 발행
  4. 예약 컨텍스트가 `BookingConfirmed` 수신 → Booking 상태 변경
- **Then**: Booking 상태가 `확정됨`이 되고 `BookingConfirmed` 이벤트가 발행된다.

---

### 시나리오 2: 확정됨 → 체크인 → 체크아웃 – 정상
- **종류**: 정상
- **Given**: 예약 B가 `확정됨` 상태이고, 체크인 날짜가 도래했다.
- **When**:
  1. 체크인 처리 요청 → 예약 컨텍스트가 상태 전이
  2. 체크아웃 날짜에 체크아웃 처리 요청 → 예약 컨텍스트가 상태 전이
- **Then**: Booking 상태가 `체크인됨` → `체크아웃됨` 순서로 전이되고 각각 `CheckInRecorded`, `CheckOutRecorded` 이벤트가 발행된다.

---

### 시나리오 3: TTL 만료 후 결제 완료 → 만료됨 – 예외
- **종류**: 예외
- **Given**: 예약 B의 선점 TTL이 만료되어 Redis 키가 자동 삭제됐다. Booking은 `요청됨` 상태다.
- **When**:
  1. 숙소 컨텍스트가 TTL 만료 감지 → `SchedulePreemptionExpired` 발행 (Kafka key: BookingID)
  2. 예약 컨텍스트가 `SchedulePreemptionExpired` 수신 → Booking 상태 → `만료됨`
  3. 결제 완료 → `TransactionCompleted` 발행 (Kafka key: BookingID, 같은 파티션으로 순서 보장)
  4. 결제 컨텍스트가 예약 컨텍스트에 유효성 체크 → `만료됨` 확인 → 결제 취소 처리
- **Then**: Booking 상태가 `만료됨`으로 최종 확정되고, 결제가 취소되며 고객에게 "예약 가능 시간 초과" 오류가 전달된다.
- **추가 메모**: `SchedulePreemptionExpired`와 `TransactionCompleted` 모두 Booking ID를 Kafka 파티션 키로 사용하여 동일 파티션 내 순서를 보장한다.

---

### 시나리오 4: 체크인 이후 취소 시도 – 예외
- **종류**: 예외
- **Given**: 예약 B가 `체크인됨` 상태다.
- **When**: 고객이 예약 취소를 요청한다.
- **Then**: 취소가 거절되고 "체크인 이후 취소 불가" 불변조건 위반 오류가 반환된다. Booking 상태는 변경되지 않는다.

---

### 시나리오 5: 체크인 없이 체크아웃 시도 – 예외
- **종류**: 예외
- **Given**: 예약 B가 `확정됨` 상태이고 체크인 처리가 되지 않았다.
- **When**: 체크아웃 처리를 요청한다.
- **Then**: 체크아웃이 거절되고 "체크인 없이 체크아웃 불가" 불변조건 위반 오류가 반환된다. Booking 상태는 변경되지 않는다.

---

### 시나리오 6: 취소된 예약 재확정 시도 – 경계
- **종류**: 경계
- **Given**: 예약 B가 `취소됨` 상태다.
- **When**: 예약 재확정을 요청한다.
- **Then**: 재확정이 거절되고 "취소된 예약 재확정 불가" 불변조건 위반 오류가 반환된다. Booking 상태는 `취소됨`을 유지한다.

---

### 시나리오 8: 결제 실패 후 TTL 내 재시도 → 확정됨 – 정상
- **종류**: 정상
- **Given**: 예약 B가 `REQUESTED` 상태이고 선점 TTL이 아직 유효하다. 첫 번째 결제 시도가 실패했다.
- **When**:
  1. 결제 실패 → `TransactionFailed` 발행 → 예약 컨텍스트 수신 (Booking `REQUESTED` 유지), 숙소 컨텍스트 선점 키 유지
  2. 고객이 TTL 만료 전 결제 재시도 → 신규 Transaction 생성 → 결제 성공 → `TransactionCompleted` 발행
  3. 이후 시나리오 1의 정상 확정 흐름으로 이어짐
- **Then**: Booking 상태가 `CONFIRMED`가 되고 선점 키가 삭제된다. 실패한 Transaction은 `FAILED` 상태로 이력에 남는다.

### 시나리오 9: 결제 실패 후 TTL 만료 → 만료됨 – 예외
- **종류**: 예외
- **Given**: 예약 B가 `REQUESTED` 상태이고 결제가 실패했다. 고객이 TTL 만료 전 재시도하지 않았다.
- **When**:
  1. 결제 실패 → 선점 키 유지
  2. TTL 만료 → `SchedulePreemptionExpired` 발행 (Kafka key: BookingID)
  3. 예약 컨텍스트 수신 → Booking 상태 `EXPIRED`
- **Then**: Booking 상태가 `EXPIRED`로 최종 확정된다. 미완료 Transaction은 `FAILED` 상태로 남는다.
- **추가 메모**: 이 경우 결제가 완료되지 않았으므로 `TransactionCompleted`가 발행되지 않아 결제 취소 처리는 불필요하다.

### 시나리오 7: TransactionCompleted 이벤트 중복 수신 – 위험
- **종류**: 위험
- **Given**: 예약 B가 이미 `확정됨` 상태이고, `BookingConfirmed` 이벤트가 정상 처리됐다.
- **When**: Kafka 재처리 등으로 `TransactionCompleted` 이벤트가 중복 수신된다.
- **Then**: 숙소 컨텍스트가 이미 확정된 일정임을 확인하고 중복 처리를 무시한다. Booking 상태 및 일정 상태는 변경되지 않는다.
- **추가 메모**: 멱등성 보장을 위해 Transaction ID 기반 중복 처리 여부를 확인해야 한다.
