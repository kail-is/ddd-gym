[STEP 1: DDD 해석]

# DDD 분석

## 체크리스트
- [x] 컨텍스트/애그리게잇/이벤트 후보가 각각 1개 이상이다.
- [x] 규칙을 정책 vs 불변조건으로 분류했다.
- [x] 트랜잭션 경계를 설명했다.
- [x] 선택한 방식과 버린 대안을 비교했다.
- [x] 트레이드오프를 명시했다.

---

## 1. Bounded Context 후보

### 예약 컨텍스트 (Booking Context)
- **책임**: 누가, 어떤 숙소의 어떤 일정을 예약하는지 관리. 예약자 정보와 예약 원장 데이터 보관.
- **핵심 용어**: 예약(Booking), 예약자(Guest), 예약 상태(BookingStatus)
- **외부 의존**: 숙소 컨텍스트에 일정 선점 요청, 결제 컨텍스트에 결제 요청

### 숙소 컨텍스트 (Accommodation Context)
- **책임**: 호스트의 숙소 정보 관리, 숙소 일정 관리, 일정 선점/충돌 방지(Redis 키) 단일 관리
- **핵심 용어**: 숙소(Accommodation), 숙소 일정(AccommodationSchedule), 선점 정책(PreemptionPolicy)
- **외부 의존**: 없음 (모든 도메인이 숙소 컨텍스트를 통해 일정 충돌 확인)

### 결제 컨텍스트 (Transaction Context)
- **책임**: 결제 금액 계산, 결제 요청/완료/실패 처리, 결제 완료 시 Kafka 이벤트 발행, 부분/전체 취소 및 환불 원장 관리
- **핵심 용어**: 거래(Transaction), 거래 상태(TransactionStatus), 거래 상세(TransactionDetail)
- **외부 의존**: 결제 완료/실패 시 숙소 컨텍스트에 선점 해제 또는 확정 요청

```
[ 예약 컨텍스트 ] ──선점 요청──▶ [ 숙소 컨텍스트 ]
       │                                  ▲
       └──결제 요청──▶ [ 결제 컨텍스트 ] ──Kafka 이벤트(확정/실패)──┘
```

---

## 2. Aggregate 후보

### Booking (예약 컨텍스트)
- **Aggregate Root**: Booking
- **포함 객체**: GuestInfo, DateRange, BookingStatus
- **책임**: 예약 상태 전이 관리, 체크인/체크아웃 불변조건 보호
- **불변조건**: 취소된 예약 재확정 불가, 체크인 이후 취소 불가

### Accommodation (숙소 컨텍스트)
- **Aggregate Root**: Accommodation
- **포함 객체**: HostInfo, AccommodationSchedule 목록, PreemptionPolicy
- **책임**: 숙소 정보 관리, 예약 완료된 일정 메타정보 관리, Redis 키 생명주기 관리
- **불변조건**: 동일 숙소·동일 일정 더블 부킹 불가

### Transaction (결제 컨텍스트)
- **Aggregate Root**: Transaction
- **포함 객체**: TransactionDetail (Entity, 1..N), TransactionStatus, BookingReference
- **책임**: 결제 처리, 완료/실패 이벤트 발행, 부분/전체 취소 원장 누적 관리, 전액 환불 시 상태 전이

#### TransactionDetail (Entity — Transaction 하위)
- **포함 정보**: 결제 상태(DetailPaymentStatus), 환불 상태(RefundStatus), 환불 금액(RefundAmount), 결제 원장 정보(LedgerInfo)
- **생성 시점**: 결제 완료 시 최초 1건 생성, 부분 취소 요청마다 환불 row 추가
- **불변조건**: TransactionDetail row는 수정 불가, 취소 이력은 append-only

#### Transaction 상태 흐름
```
결제완료(Paid) → 부분취소(PartialCancelled) → 전체취소(FullyCancelled)
```
- `PartialCancelled`: 부분 취소 발생 시, TransactionDetail에 환불 row 추가
- `FullyCancelled`: 잔여 결제 금액이 0이 되는 시점에 Transaction 상태 전이

---

## 3. Domain Event 후보

| 이벤트 | 발생 시점 | 발행 주체 | 수신 주체 |
|--------|-----------|-----------|-----------|
| `SchedulePreempted` (일정 선점됨) | 예약하기 클릭 시 | 숙소 컨텍스트 | - |
| `BookingInitiated` (예약 시작됨) | 예약 데이터 입력 시작 | 예약 컨텍스트 | - |
| `TransactionCompleted` (결제 완료됨) | 결제 성공 시 | 결제 컨텍스트 | 숙소 컨텍스트 |
| `TransactionFailed` (결제 실패됨) | 결제 실패 시 | 결제 컨텍스트 | 숙소 컨텍스트 |
| `BookingConfirmed` (예약 확정됨) | 결제 완료 후 일정 확정 시 | 숙소 컨텍스트 | 예약 컨텍스트 |
| `PartialRefundProcessed` (부분 환불 처리됨) | 부분 취소 요청 처리 시 | 결제 컨텍스트 | 예약 컨텍스트 |
| `TransactionFullyRefunded` (전액 환불 완료됨) | 잔여 결제금액이 0이 되는 시점 | 결제 컨텍스트 | 예약 컨텍스트, 숙소 컨텍스트 |
| `SchedulePreemptionExpired` (선점 만료됨) | TTL 만료 시 | 숙소 컨텍스트 | - |
| `BookingCancelled` (예약 취소됨) | 취소 요청 처리 시 | 예약 컨텍스트 | 숙소 컨텍스트, 결제 컨텍스트 |
| `CheckInRecorded` (체크인 완료됨) | 체크인 처리 시 | 예약 컨텍스트 | - |
| `CheckOutRecorded` (체크아웃 완료됨) | 체크아웃 처리 시 | 예약 컨텍스트 | - |

---

## 4. 엔티티 / Value Object 후보

### 엔티티 (Entity)
| 이름 | 컨텍스트 | 책임 |
|------|----------|------|
| `Booking` | 예약 | 예약 원장, 상태 전이 관리 |
| `Accommodation` | 숙소 | 숙소 정보, 일정/선점 정책 관리 |
| `AccommodationSchedule` | 숙소 | 확정된 예약 일정 메타정보 |
| `Transaction` | 결제 | 결제/환불 원장 관리, 상태 전이 (Paid → PartialCancelled → FullyCancelled) |
| `TransactionDetail` | 결제 | 결제·환불 row 이력, 원장 정보 (Transaction 하위 Entity) |

### Value Object (VO)
| 이름 | 컨텍스트 | 설명 |
|------|----------|------|
| `DateRange` | 예약, 숙소 | 체크인~체크아웃 날짜 범위 |
| `GuestInfo` | 예약 | 예약자 이름, 연락처, 인원 수 |
| `ScheduleKey` | 숙소 | Redis 키 복합값 (숙소ID + DateRange) |
| `PreemptionPolicy` | 숙소 | 선점 TTL, 숙소/이벤트별 정책 |
| `RefundAmount` | 결제 | 환불 금액, 통화, 환불 사유 (TransactionDetail 내 VO) |
| `LedgerInfo` | 결제 | 결제 원장 정보, PG사 거래 ID, 승인 번호 (TransactionDetail 내 VO) |

---

## 5. 규칙 분류

### 불변조건 (Invariant) - Aggregate에서 강제
- 동일 숙소·동일 일정에 확정된 예약이 두 개 이상 존재할 수 없다.
- 취소된 예약(`BookingCancelled`)은 재확정할 수 없다.
- 체크인(`CheckInRecorded`) 이후 취소할 수 없다.
- 체크인 없이 체크아웃할 수 없다.

### 정책 (Policy) - 이벤트/워크플로우에서 처리
- 선점 TTL 기본값: 1시간 (숙소별, 이벤트 기간별 조절 가능)
- 결제 실패 시 숙소 도메인에 명시적 선점 삭제 요청
- TTL 만료 시 선점 자동 해제 (Redis 자동 만료)
- 체크인 전 취소 수수료 정책 (숙소/호스트별 상이)

---

## 6. 트랜잭션 경계

### 선점 트랜잭션 (동기)
```
고객 "예약하기" 클릭
  → 예약 컨텍스트: 예약 데이터 생성
  → 숙소 컨텍스트: Redis 키 존재 확인 + 생성 (원자적)
  → 충돌 없으면: 선점 성공 → 예약 진행
  → 충돌 있으면: "이미 예약 중인 일정" 응답
```

### 확정 트랜잭션 (비동기 - Kafka)
```
결제 컨텍스트: TransactionCompleted 이벤트 발행
  → Kafka
  → 숙소 컨텍스트: 일정 "예약완료" 상태 생성
                    Redis 키 삭제
  → BookingConfirmed 이벤트 발행
  → 예약 컨텍스트: 예약 상태 "확정됨"으로 변경
```

### 실패 트랜잭션 (동기)
```
결제 컨텍스트: 결제 실패 감지 → TransactionFailed 발행
  → 숙소 컨텍스트: Redis 키 삭제 요청
  → 선점 해제 완료
```

### 부분 취소 트랜잭션 (동기)
```
고객: 부분 취소 요청 (특정 박 수 선택)
  → 결제 컨텍스트: TransactionDetail에 환불 row 추가
                    Transaction 상태 → PartialCancelled
  → PartialRefundProcessed 발행
  → 예약 컨텍스트: 예약 DateRange 조정
```

### 전액 취소 트랜잭션 (동기)
```
잔여 결제 금액 = 0 확인
  → 결제 컨텍스트: Transaction 상태 → FullyCancelled
  → TransactionFullyRefunded 발행
  → 숙소 컨텍스트: 일정 선택가능 상태 복원
  → 예약 컨텍스트: 예약 상태 → 취소됨
```

---

## 7. 선택한 방식 vs 버린 대안

### 선택: 숙소 도메인이 Redis 충돌 방지 계층 단일 관리

**이유**: 모든 예약 채널(자사 앱, OTA, 전화 예약)이 숙소 도메인을 통해 일정 충돌을 확인하므로 단일 진실 소스(Single Source of Truth) 보장. TTL 정책도 숙소 도메인이 직접 관리하여 일관성 유지.

### 버린 대안 1: 예약 도메인이 직접 중복 체크
- **이유**: 예약 도메인이 숙소 일정 데이터에 직접 의존하게 되어 컨텍스트 경계 침범. 여러 채널에서 인입 시 단일 소스 보장 어려움.

### 버린 대안 2: DB 비관적 락(Pessimistic Lock)으로 처리
- **이유**: 성능 병목 발생. 동시 예약 요청이 많은 인기 숙소에서 처리량 저하. 분산 환경에서 락 관리 복잡도 증가.

### 버린 대안 3: 숙소 도메인에서 DB로만 선점 상태 관리
- **이유**: Redis 없이 DB로만 관리 시 Check-Then-Act 사이의 경쟁 조건 해결 어려움. 응답 속도 저하.

---

## 8. 트레이드오프

| 결정 | 얻는 것 | 잃는 것 |
|------|---------|---------|
| Redis를 충돌 방지 단일 계층으로 사용 | 빠른 선점 처리, 자동 TTL 만료 | Redis 장애 시 예약 기능 전체 영향 |
| 숙소 도메인이 Redis 단일 관리 | 책임 명확, 단일 진실 소스 | 숙소 도메인의 역할 비대화 가능성 |
| Kafka 비동기 이벤트로 확정 처리 | 결제/숙소 도메인 간 결합도 감소 | 이벤트 유실/중복 수신 시 멱등성 처리 필요 |
| TTL을 숙소별 정책으로 분리 | 인기 숙소 공실 최소화 | 정책 관리 복잡도 증가 |

---

## 열린 질문 (Week2로 이월)

1. Redis 장애 시 Fallback 전략은 무엇인가? 예약 기능 전체 차단 vs DB Fallback
2. Kafka 이벤트 중복 수신 시 `BookingConfirmed` 멱등성을 어떻게 보장하는가?
3. 호스트가 이미 선점 중인 일정을 강제 차단할 경우 선점 고객에게 어떻게 보상하는가?
4. 외부 OTA 채널의 예약이 숙소 도메인을 거치지 않고 직접 인입되는 경우의 처리 방식은?