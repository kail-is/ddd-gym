[STEP 1: DDD 해석]

# DDD 분석 템플릿
요구사항을 해석해 설계 후보를 만들고, 선택/트레이드오프를 명시합니다.

## 체크리스트
- [x] 컨텍스트/애그리게잇/이벤트 후보가 각각 1개 이상이다.
- [x] 규칙을 정책 vs 불변조건으로 분류했다.
- [x] 트랜잭션 경계를 설명했다.
- [x] 선택한 방식과 버린 대안을 비교했다.
- [x] 트레이드오프를 명시했다.
- [x] (Week2) Aggregate 책임 상세화
- [x] (Week2) Domain Event 목록 확정
- [x] (Week2) 불변조건 보호 방법 명시
- [x] (Week2) 트레이드오프 섹션 업데이트

## 문서 구조

### 1. Bounded Context 후보

| 컨텍스트 | 주체 | 책임 |
|---------|------|------|
| **숙소 컨텍스트** | 호스트 | 숙소 정보 관리, 가용일 관리 |
| **예약 컨텍스트** | 고객 | 상태 흐름 관리 (요청→확정→체크인→체크아웃, 취소) |
| **결제 컨텍스트** | 고객 | 결제 처리, 환불 처리 |

### 2. Aggregate 후보

| Aggregate | 루트 엔티티 | 책임 |
|-----------|------------|------|
| 숙소 | Accommodation | 숙소 정보 관리, 날짜별 가용일 관리, 가용일 잠금/해제 |
| 예약 | Booking | 예약 상태 관리, 상태 전이 규칙 보장, heartbeat 잠금 관리, 환불 금액 계산 |
| 결제 | Payment | PG 통신 (결제/환불 실행). BFF성 역할만 담당 |

#### (Week2) Aggregate 책임 상세화

**Booking Aggregate**
- 상태 전이: `요청됨 → 확정됨/거절됨 → 체크인 → 체크아웃`, `확정됨 → 취소됨`
- Booking은 자기 상태 전이만 책임. 취소 요청 존재 여부는 모름
- heartbeat 폴링 잠금: `lastHeartbeat` 기록, 폴링 끊김 + 10분 경과 시 잠금 해제 + 요청 삭제 (상태 전이가 아님)
- 이선좌: `요청됨` 상태에서 해당 날짜를 잠금, 다른 사용자 예약 요청 거절
- 환불 금액 계산: 취소 시점과 사유에 따른 환불 금액을 직접 계산하여 결제 컨텍스트에 전달
- (학습 목적) 부분 취소 시: 내부에 날짜별 `BookingItem`을 두고, 개별 아이템 취소 가능. 전체 아이템 취소 시 Aggregate Root가 자기 상태를 `취소됨`으로 전이

**CancellationRequest (독립 엔티티, 큐 테이블)**
- Booking 밖에서 관리되는 취소 요청 대기열. 히스토리성 테이블
- 고객이 취소 요청 → CancellationRequest 생성 (Booking 상태 변경 없음)
- 호스트가 확정 → `CancellationApproved` 이벤트 발행 → Booking.cancel() 호출
- 호스트가 거절 → 요청 처리 완료, Booking은 아무것도 모름
- Aggregate가 아닌 이유: 내부에 보호할 불변조건이나 하위 객체가 없음. 생성 → 확정/거절로 끝나는 단순 lifecycle
- 독립 조회/저장 필요: 호스트가 대기 중인 취소 요청을 API로 조회해야 하므로 별도 저장소

**취소 요청 중 체크인 방지 (서비스 레벨 보호)**
- "취소 요청 중엔 체크인 불가" 규칙은 Booking 엔티티가 아닌 애플리케이션 서비스에서 보호
- 체크인 서비스가 CancellationRequest 큐를 확인 → 대기 중인 요청이 있으면 체크인 거절
- 설계 결정 이유: Booking 엔티티에 취소 요청 필드를 넣으면 관심사가 섞이고 온보딩 시 설명 비용 증가
- 트레이드오프: 서비스를 우회하면 불변조건이 뚫릴 수 있음. 하지만 엔티티에 넣어도 휴먼에러 확률은 비슷하므로 엔티티 깔끔함을 선택
- 보완: Kotlin `internal` 가시성 등으로 서비스 외부에서 직접 호출 제한 가능

**Payment Aggregate**
- 역할 축소: 환불 금액 계산은 예약 컨텍스트가 담당. Payment는 PG사 API 통신만 수행
- 결제 실행, 전액/부분 환불 실행

**Accommodation Aggregate**
- 날짜별 가용일 관리 (부분 취소 지원 시 날짜별 × 호수/수량별)
- `BookingExpired` 구독 → 가용일 복구
- 부분 취소 시 해당 날짜 가용일 복구

### 3. Domain Event 후보

**숙소 컨텍스트 발행:**
- `AccommodationDeactivated` - 숙소 비활성화됨

**예약 컨텍스트 발행:**
- `BookingRequested` - 예약 요청됨
- `BookingConfirmed` - 예약 확정됨
- `BookingRejected` - 예약 거절됨 (호스트가 확정 전 거절)
- `CancellationRequested` - 취소 요청됨 (고객이 요청, Booking 상태 변경 없음)
- `CancellationApproved` - 취소 확정됨 (호스트가 승인 → Booking.cancel() 트리거)
- `CancellationRejected` - 취소 거절됨 (호스트가 거절, Booking 영향 없음)
- `BookingCancelled` - 예약 취소됨 (CancellationApproved 후 발행)
- `CheckedIn` - 체크인 완료
- `CheckedOut` - 체크아웃 완료

**예약 컨텍스트 구독:**
- `AccommodationDeactivated` → 해당 숙소 예약 취소 (BookingCancelled 발행)

**예약 컨텍스트 발행 (Week2 추가):**
- `BookingExpired` - 잠금 해제됨 (heartbeat 폴링 끊김 + 10분 경과, 예약 성립 전 삭제)

**결제 컨텍스트 구독:**
- `BookingRequested` → 결제 처리
- `BookingRejected` → 전액 환불
- `BookingCancelled` → 조건부 환불 (예약 컨텍스트가 계산한 금액으로)

**숙소 컨텍스트 구독 (Week2 추가):**
- `BookingExpired` → 가용일 복구
- 부분 취소 시 → 해당 날짜 가용일 복구

### 4. 엔티티 / Value Object 후보

**엔티티:**
- `Accommodation` - 숙소 (정보, 가용일)
- `Booking` - 예약 (상태, 고객, 숙소, 날짜 등)
- `Payment` - 결제 (금액, 상태, 환불 내역)
- `CancellationRequest` - 취소 요청 (요청자, 사유, 처리 상태). 큐/히스토리성 독립 엔티티

**Value Object:**
- `DateRange` - 체크인/체크아웃 날짜 범위
- `GuestCount` - 인원 수
- `CancellationReason` - 취소 사유 (주체, 사유 내용)
- `Money` - 금액

### 5. 규칙 분류

**불변조건 (깨지면 안 됨):**
- 취소된 예약은 재확정 불가
- 체크인 이후 취소 불가
- 체크인 없이 체크아웃 불가
- (Week2) 이선좌 잠금 중인 날짜에 다른 사용자 예약 불가
- (Week2) 잠금 해제 후 재시도 불가 (새로 요청해야 함)
- (Week2) 취소 요청 대기 중에는 체크인 불가

**불변조건 보호 방법 (Week2):**
- 상태 전이 규칙: Booking Aggregate Root의 `require` 검증으로 보호 (코드에 이미 반영)
- 이선좌 잠금: heartbeat 폴링으로 잠금 유지, `lastHeartbeat`로 해제 시점 판단
- 부분 취소 시 Booking 상태: Aggregate Root가 내부 BookingItem 전체 상태를 확인하여 자기 상태 전이 결정
- 취소 요청 중 체크인 방지: 애플리케이션 서비스에서 CancellationRequest 큐 확인 후 체크인 허용/거절 (엔티티 레벨이 아닌 서비스 레벨 보호)

**정책 (변경 가능):**
- 취소 시점에 따른 환불 비율
- 취소 주체에 따른 페널티/보상
- (Week2) heartbeat 폴링 끊김 후 유예 시간 (현재 10분)
- (Week2) 부분 취소 지원 여부 (현재 미지원 결정)

### 6. 전체 워크플로우

**정상 플로우:**
```
예약 요청 → BookingRequested → 결제 처리
    ↓
호스트 확정/거절
    ↓
┌─ BookingConfirmed → 체크인 대기 → CheckedIn → CheckedOut → 완료
└─ BookingRejected → 전액 환불
```

**취소 플로우 (확정 후, 체크인 전):**
```
고객 취소 요청 → CancellationRequest 생성 (Booking 상태 변경 없음)
    ↓
호스트 확정/거절
    ↓
┌─ CancellationApproved → Booking.cancel() → BookingCancelled → 조건부 환불
└─ CancellationRejected → Booking 영향 없음, 요청 처리 완료
```

※ 숙소가 직접 취소하는 경우: CancellationRequest 없이 바로 Booking.cancel()

**숙소 비활성화 플로우:**
```
AccommodationDeactivated → 해당 예약 취소 → BookingCancelled → 환불
```

### 7. 트랜잭션 경계

- **예약 컨텍스트 내**: 상태 전이는 단일 트랜잭션
- **컨텍스트 간**: 이벤트로 연결 (최종 일관성)
  - 예: 취소됨 이벤트 발행 → 결제 컨텍스트가 구독하여 환불 처리

### 8. 선택한 방식 vs 버린 대안

| 선택 | 버린 대안 |
|------|----------|
| 예약/결제 컨텍스트 분리 | 단일 컨텍스트에서 모두 처리 |
| 이벤트 기반 연결 | 동기 호출로 강결합 |
| 취소 시 사유 기록 | 상태만 변경 |

**분리 이유:** 강결합 방지, 책임 명확화

### 9. 트레이드오프

| 결정 | 장점 | 단점 |
|------|------|------|
| 컨텍스트 분리 | 느슨한 결합, 독립 배포 가능 | 운영 복잡도 증가 |
| 이벤트 연결 | 확장성 | 환불 실패 시 재처리 필요 |
| 취소 사유 기록 | 추적 가능 | 저장 공간, 복잡도 증가 |

#### (Week2) 트레이드오프 업데이트

| 결정 | 장점 | 단점 |
|------|------|------|
| heartbeat 폴링 잠금 (vs 서버 TTL) | 사용자 이탈 빠르게 감지, 가용일 빠른 해제로 예약 기회 증가 | 폴링 인프라 필요, 서버 TTL만으로도 동작은 함 |
| 부분 취소 미지원 (vs BookingItem 분해) | 설계 단순, DateRange VO 유지 | 고객이 전체 취소 → 재예약 사이에 날짜 뺏길 수 있음 |
| 환불 계산을 예약 컨텍스트가 담당 (vs 결제 컨텍스트) | 결제 컨텍스트가 PG 통신에만 집중, 비즈니스 로직 분리 | 예약 컨텍스트 책임 증가 |
| BookingExpired 이벤트 추가 (vs PaymentFailed) | 다른 컨텍스트는 만료 사실만 알면 됨, 결제 실패 상세는 캡슐화 | 결제 실패 원인 추적 시 결제 컨텍스트 직접 조회 필요 |
| CancellationRequest를 Booking 밖 독립 엔티티로 분리 (vs Booking 내부 필드) | Booking 엔티티 깔끔, 관심사 분리, 온보딩 설명 비용 감소 | 체크인 방지 불변조건이 서비스 레벨에 있어 우회 가능성 |
| 취소 요청 시 Booking 상태 변경 안 함 (vs 취소요청됨 상태 추가) | 상태 전이 단순, 취소 확정 시에만 변경 | 서비스에서 큐 확인 로직 필요 |
| 취소 확정/거절을 호스트가 판단 (vs 자동 처리) | 실제 숙박 비즈니스 반영, 호스트 재량 | 처리 대기 시간 발생, 체크인 임박 시 문제 |

> TIP: AI에게는 "정답을 말하지 말고, 이 설계의 경계/책임 약점을 질문으로 드러내달라"고 요청하세요.
