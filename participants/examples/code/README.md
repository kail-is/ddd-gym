# 코드 예시

> 이 폴더의 코드는 완전한 구현이 아닌 **설계 의도를 보여주는 스케치**입니다.

## 언어별 예시

| 파일 | 언어 | 특징 |
|------|------|------|
| `booking_kotlin.kt` | Kotlin | data class, sealed interface, 간결한 문법 |
| `booking_java.java` | Java 17+ | record, sealed interface, 전통적 DDD |
| `booking_typescript.ts` | TypeScript | 타입 안전성, Union 타입 이벤트 |

## 공통 구조

### Value Objects
- `StayPeriod`: 예약 기간 (checkInDate, checkOutDate)
- `GuestCount`: 인원 수 (adults, children)
- `BookingId`: 예약 식별자

### Entity / Aggregate Root
- `Booking`: 예약 (Aggregate Root)
  - 상태: REQUESTED → CONFIRMED → CHECKED_IN → CHECKED_OUT
  - 또는: REQUESTED → CANCELLED, CONFIRMED → CANCELLED

### Domain Events
- `BookingRequested`: 예약 요청됨
- `BookingConfirmed`: 예약 확정됨
- `BookingCancelled`: 예약 취소됨
- `CheckedIn`: 체크인됨
- `CheckedOut`: 체크아웃됨

## 불변조건 (Invariants)

코드에서 표현된 핵심 불변조건:

1. **취소된 예약은 다시 확정할 수 없다**
   ```kotlin
   check(status != BookingStatus.CANCELLED) {
       "취소된 예약은 다시 확정할 수 없습니다"
   }
   ```

2. **체크인 이후에는 취소할 수 없다**
   ```kotlin
   check(status != BookingStatus.CHECKED_IN && status != BookingStatus.CHECKED_OUT) {
       "체크인 이후에는 취소할 수 없습니다"
   }
   ```

3. **체크인 없이 체크아웃할 수 없다**
   ```kotlin
   check(status == BookingStatus.CHECKED_IN) {
       "체크인된 예약만 체크아웃할 수 있습니다"
   }
   ```

## 참가자 작성 가이드

코드를 직접 작성할 때:

1. **Value Object부터 시작**
   - 도메인 개념을 값으로 표현
   - 불변성 유지
   - 유효성 검증을 생성자에서

2. **Aggregate Root 정의**
   - 상태 변경은 메서드를 통해서만
   - 불변조건을 메서드 시작에서 검증
   - 이벤트 발행

3. **Domain Event 설계**
   - 상태 변화마다 이벤트 생성
   - 이벤트는 불변 (immutable)

## 코드 작성 위치

자신의 코드는 `participants/<이름>/code/` 폴더에 저장하세요.

```
participants/<이름>/
└── code/
    ├── booking.kt (또는 .java, .ts)
    └── README.md (선택)
```
