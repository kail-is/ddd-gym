# 대실 예약 시스템 - DDD 설계 코드 (TypeScript)

> 이 폴더의 코드는 완전한 구현이 아닌 **설계 의도를 보여주는 스케치**입니다.
> Week1 DDD 분석을 바탕으로 작성된 예약 관리 컨텍스트의 도메인 모델입니다.

---

## 파일 구조

```
code/
  DomainEvent.ts              ← 도메인 이벤트 기본 인터페이스
  ReservationStatus.ts        ← 상태 열거형 (5가지)
  TimeSlot.ts                 ← Value Object: 시간대
  PriceSnapshot.ts            ← Value Object: 예약 금액 스냅샷
  ReservationEvents.ts        ← 도메인 이벤트 5종
  ReservationOverlapChecker.ts ← Domain Service: 중복 예약 확인
  Reservation.ts              ← Aggregate Root
```

---

## Bounded Context

이 코드는 4개 컨텍스트 중 **예약 관리** 컨텍스트만 다룹니다.

| 컨텍스트 | 책임 |
|---------|------|
| **예약 관리** (이 코드) | 예약 생성/확정/취소/체크인/체크아웃 |
| 숙소 관리 | 숙소 정보, 가격 정책 |
| 정산 관리 | 예약/취소 금액 기록 |
| 결제 관리 | 결제 처리 |

---

## 상태 흐름

```
요청됨(REQUESTED)
  ├── → 확정됨(CONFIRMED)     3시간 후 스케줄링 시스템이 자동 전환
  │     ├── → 취소됨(CANCELLED)   고객/호스트 취소 (취소 비용/보상 발생)
  │     ├── → 체크인(CHECKED_IN)
  │     │     └── → 체크아웃(CHECKED_OUT)
  └── → 취소됨(CANCELLED)     요청됨 단계 취소 (수수료 없음)
```

---

## Value Objects

### TimeSlot — 시간대
```typescript
const slot = new TimeSlot(
  new Date('2026-04-13T14:00:00'),
  new Date('2026-04-13T17:00:00'),
)

slot.overlaps(other)  // 시간대 중복 여부 판단
slot.isPast()         // 이미 지난 시간대 여부
slot.equals(other)    // 동일 시간대 비교
```

### PriceSnapshot — 예약 금액 스냅샷
```typescript
const price = new PriceSnapshot(50000, 'KRW')
```
- 숙소 관리 컨텍스트의 가격 정책을 **예약 생성 시점에 고정** 저장
- 이후 가격 정책이 변경돼도 예약 금액은 불변

---

## Aggregate Root — Reservation

### 생성
```typescript
const reservation = await Reservation.create(
  {
    id: 'res-001',
    roomId: 'room-123',       // 숙소 관리 컨텍스트 참조값
    customerId: 'cust-456',   // 고객 테이블 참조값
    timeSlot: new TimeSlot(start, end),
    guestCount: 2,
    priceSnapshot: new PriceSnapshot(50000),
  },
  overlapChecker,             // Domain Service 주입
)
```

### 상태 전환 메서드

| 메서드 | 전환 | 검증 |
|--------|------|------|
| `confirm()` | REQUESTED → CONFIRMED | REQUESTED 상태만 허용 |
| `cancelByCustomer()` | REQUESTED/CONFIRMED → CANCELLED | CHECKED_IN/OUT 거부 |
| `cancelByHost(hostId)` | REQUESTED/CONFIRMED → CANCELLED | CHECKED_IN/OUT 거부 |
| `checkIn()` | CONFIRMED → CHECKED_IN | CONFIRMED 상태만 허용 |
| `checkOut()` | CHECKED_IN → CHECKED_OUT | CHECKED_IN 상태만 허용 |

---

## 불변조건 (Invariants)

코드에서 Reservation이 스스로 지키는 5가지 불변조건:

**1. 취소된 예약은 재확정 불가**
```typescript
// confirm()
if (this._status !== ReservationStatus.REQUESTED) {
  throw new Error('요청됨 상태에서만 확정할 수 있습니다.')
}
```

**2. 체크인 이후 취소 불가**
```typescript
// cancelByCustomer() / cancelByHost()
if (this._status === ReservationStatus.CHECKED_IN ||
    this._status === ReservationStatus.CHECKED_OUT) {
  throw new Error('체크인 이후에는 취소할 수 없습니다.')
}
```

**3. 체크인 전 체크아웃 불가**
```typescript
// checkOut()
if (this._status !== ReservationStatus.CHECKED_IN) {
  throw new Error('체크인 이후에만 체크아웃할 수 있습니다.')
}
```

**4. 중복 시간대 예약 불가**
```typescript
// Reservation.create()
const hasOverlap = await overlapChecker.hasOverlap(props.roomId, props.timeSlot)
if (hasOverlap) {
  throw new Error('해당 시간대에 이미 예약이 존재합니다.')
}
```

**5. 이미 지난 시간대 예약 불가**
```typescript
// Reservation.create()
if (props.timeSlot.isPast()) {
  throw new Error('이미 지나간 시간대는 예약할 수 없습니다.')
}
```

---

## Domain Service — ReservationOverlapChecker

```typescript
export interface ReservationOverlapChecker {
  hasOverlap(roomId: string, timeSlot: TimeSlot): Promise<boolean>
}
```

단일 `Reservation`은 다른 예약의 존재를 알 수 없으므로 도메인 서비스로 분리.
구현체는 인프라 레이어에서 Repository를 통해 여러 Reservation을 조회해 판단.

---

## Domain Events

상태 변화가 일어날 때마다 이벤트가 발행됩니다.

| 이벤트 | 발행 시점 | 정산 처리 |
|--------|----------|----------|
| `ReservationConfirmed` | 예약 확정 | 예약 금액 기록 |
| `CustomerReservationCancelled` | 고객 취소 | 체크인 날짜 기준 취소 비용 |
| `HostReservationCancelled` | 호스트 취소 | 예약일 기준 고객 보상 |
| `ReservationCheckedIn` | 체크인 | - |
| `ReservationCheckedOut` | 체크아웃 | - |

### 이벤트 수집 및 발행
```typescript
reservation.cancelByCustomer()

// 트랜잭션 커밋 후 이벤트 발행
const events = reservation.pullDomainEvents()
// → [CustomerReservationCancelled]
// → 정산 시스템으로 전달 (별도 트랜잭션)
```

> 예약 관리(단일 트랜잭션)와 정산 관리(이벤트 기반)는 별도 컨텍스트이므로
> 두 시스템 간 일관성은 이벤트로 보장합니다.
