# 숙박 예약 서비스 (Accommodation Booking Service)

DDD(Domain-Driven Design) 기반으로 설계된 숙박 예약 시스템입니다.  
Accommodation, Booking, Transaction 세 개의 바운디드 컨텍스트로 분리되어 있으며,  
Redis 선점 메커니즘과 Kafka 이벤트 기반 통신으로 동시성과 컨텍스트 간 결합도를 관리합니다.

---

## 목차

1. [프로젝트 구조](#프로젝트-구조)
2. [기술 스택](#기술-스택)
3. [아키텍처 개요](#아키텍처-개요)
4. [바운디드 컨텍스트](#바운디드-컨텍스트)
5. [유비쿼터스 랭귀지](#유비쿼터스-랭귀지)
6. [서비스 규칙](#서비스-규칙)
7. [예약 상태 흐름](#예약-상태-흐름)
8. [선점 정책 (PreemptionPolicy)](#선점-정책-preemptionpolicy)
9. [API 엔드포인트](#api-엔드포인트)
10. [FE 화면 구성](#fe-화면-구성)
11. [인프라 구성](#인프라-구성)
12. [실행 방법](#실행-방법)

---

## 프로젝트 구조

```
yongchul/
├── accommodation-booking-service/   # 백엔드 (Spring Boot / Kotlin)
└── accommodation-booking-web/       # 프론트엔드 (React / TypeScript)
```

---

## 기술 스택

### 백엔드 (accommodation-booking-service)

| 분류 | 기술 | 버전 |
|------|------|------|
| 언어 | Kotlin | 1.9.23 |
| 런타임 | Java | 21 |
| 프레임워크 | Spring Boot | 3.2.5 |
| ORM | Spring Data JPA | - |
| DB | PostgreSQL | 16 (local) |
| 캐시 / 선점 | Spring Data Redis | - |
| 메시징 | Spring Kafka | - |
| JSON | Jackson + JavaTimeModule | - |
| 빌드 | Gradle (Kotlin DSL) | 8.x |

### 프론트엔드 (accommodation-booking-web)

| 분류 | 기술 | 버전 |
|------|------|------|
| 언어 | TypeScript | 5.6 |
| UI 프레임워크 | React | 18.3 |
| 빌드 도구 | Vite | 8.0 |
| 라우팅 | React Router DOM | 6.27 |
| 데이터 페칭 | TanStack React Query | 5.59 |
| 폼 관리 | React Hook Form | 7.53 |
| 스키마 검증 | Zod | 3.23 |
| HTTP 클라이언트 | Axios | 1.7 |
| 스타일링 | Tailwind CSS | 3.4 |
| 날짜 처리 | dayjs | 1.11 |
| 아이콘 | Lucide React | 0.454 |

---

## 아키텍처 개요

```
[FE: React]
     │  HTTP (REST API)
     ▼
[BE: Spring Boot]
     │
     ├── Accommodation Context
     │     ├── PostgreSQL  (Accommodation, Room, RoomSchedule, ConfirmedBookingDate)
     │     └── Redis       (선점 키 TTL 관리)
     │
     ├── Booking Context
     │     └── PostgreSQL  (BookingOrder, BookingOrderLineItem)
     │
     └── Transaction Context
           └── PostgreSQL  (Transaction, TransactionDetail)

[Kafka] ←→ 컨텍스트 간 이벤트 통신
[Redis Keyspace Notification] → 선점 TTL 만료 감지
```

### 헥사고날 아키텍처 (각 컨텍스트)

```
adapter/in/web       ← REST Controller
adapter/in/kafka     ← Kafka Consumer
application/port/in  ← UseCase Interface
application/service  ← 비즈니스 로직
application/port/out ← Port Interface
adapter/out/persistence ← JPA Repository
adapter/out/redis    ← Redis Adapter
domain/              ← Entity, VO, Event (순수 도메인)
```

---

## 바운디드 컨텍스트

### 1. Accommodation Context (숙소 관리)

숙소와 객실의 등록 및 예약 불가 일정을 관리합니다.

**주요 도메인 객체**

| 객체 | 유형 | 설명 |
|------|------|------|
| `Accommodation` | Aggregate Root | 숙소 정보 (이름, 주소, 호스트, 선점 정책) |
| `Room` | Aggregate Root | 객실 정보 (수용 인원, 1박 가격, 선점 정책) |
| `RoomSchedule` | Entity | 호스트가 설정한 예약 불가 일정 |
| `ConfirmedBookingDate` | Entity | 결제 확정된 예약 날짜 (예약 불가 기준) |
| `PreemptionPolicy` | Value Object | 리드타임 기반 선점 TTL 정책 |

### 2. Booking Context (예약 관리)

고객의 예약 주문 생명주기를 관리합니다.

**주요 도메인 객체**

| 객체 | 유형 | 설명 |
|------|------|------|
| `BookingOrder` | Aggregate Root | 예약 주문 (상태, 예약자 정보, 만료 시각) |
| `BookingOrderLineItem` | Entity | 예약 항목 (숙소/객실 스냅샷, 날짜, 금액) |
| `GuestInfo` | Value Object | 예약자 정보 (이름, 연락처, 인원) |
| `AccommodationSnapshot` | Value Object | 예약 시점의 숙소 정보 스냅샷 |
| `RoomSnapshot` | Value Object | 예약 시점의 객실 정보 스냅샷 |

### 3. Transaction Context (결제 관리)

결제 처리 및 환불 내역을 관리합니다.

**주요 도메인 객체**

| 객체 | 유형 | 설명 |
|------|------|------|
| `Transaction` | Aggregate Root | 결제 정보 (상태, 총액) |
| `TransactionDetail` | Entity | 결제 세부 내역 (완료, 취소, 부분 환불) |
| `LedgerInfo` | Value Object | 원장 정보 (결제 수단, 거래 ID) |
| `RefundAmount` | Value Object | 환불 금액 정보 |

---

## 유비쿼터스 랭귀지

| 용어 | 설명 |
|------|------|
| **숙소 (Accommodation)** | 호스트가 등록한 숙박 시설 |
| **객실 (Room)** | 숙소에 속한 예약 가능 단위 |
| **예약 주문 (BookingOrder)** | 고객이 생성한 예약 요청 단위 |
| **선점 (Preemption)** | Redis SETNX로 특정 날짜를 임시 점유하는 행위. TTL 동안 타 고객의 동일 날짜 예약을 방지 |
| **선점 정책 (PreemptionPolicy)** | 체크인 리드타임에 따라 선점 TTL을 차등 적용하는 규칙 |
| **리드타임 (Lead Time)** | 현재 날짜와 체크인 날짜 사이의 일수 |
| **TTL (Time To Live)** | Redis 선점 키의 유효 시간. 만료 시 예약 주문이 EXPIRED 전이 |
| **예약 불가 날짜** | 호스트 차단일(`RoomSchedule`) + 확정 예약일(`ConfirmedBookingDate`)의 합집합 |
| **차단 일정 (BlockedSchedule)** | 호스트가 직접 설정한 예약 불가 기간 (점검, 호스트 사용 등) |
| **확정 예약일 (ConfirmedBookingDate)** | 결제까지 완료된 예약의 투숙 날짜 |
| **스냅샷 (Snapshot)** | 예약 시점의 숙소/객실 정보 복사본. 이후 정보가 변경되어도 예약 당시 기준 유지 |
| **결제 확정 (Confirm)** | 고객이 결제를 완료하고 예약이 CONFIRMED 상태로 전이하는 행위 |
| **선점 해제 (Release)** | 취소 또는 만료로 인해 선점 점유가 해제되는 행위 |

---

## 서비스 규칙

### 예약 불변 조건

| 규칙 | 설명 |
|------|------|
| 재확정 불가 | 취소된 예약(CANCELLED)은 다시 확정할 수 없다 |
| 체크인 후 취소 불가 | CHECKED_IN 이후 상태에서는 취소할 수 없다 |
| 순차 체크아웃 | 체크인 없이 체크아웃할 수 없다 |
| 만료 후 확정 불가 | EXPIRED 상태의 예약은 결제 확정할 수 없다 |

### 예약 불가 날짜 규칙

| 규칙 | 설명 |
|------|------|
| 호스트 차단 | 호스트가 설정한 날짜는 예약 불가 |
| 확정 예약 | 다른 고객이 결제 완료한 날짜는 예약 불가 |
| 달력 표시 | 게스트 예약 화면의 달력에서 예약 불가 날짜는 선택 불가 |
| 범위 차단 | 체크인 선택 후, 이후 첫 번째 예약 불가 날짜까지만 체크아웃 선택 가능 |

### 선점 및 동시성 규칙

| 규칙 | 설명 |
|------|------|
| SETNX 선점 | 예약 요청 시 Redis SETNX로 원자적 점유. 이미 점유된 날짜는 예약 불가 |
| DB 충돌 사전 확인 | Redis 캐시 미스 대비, 선점 전 `ConfirmedBookingDate` 충돌 여부를 DB에서 먼저 확인 |
| TTL 만료 자동 처리 | 결제 미완료 시 Redis TTL 만료 → Keyspace Notification → 예약 주문 EXPIRED 전이 |
| 확정 후 TTL 연장 | 결제 확정 시 선점 키 TTL을 400일로 연장하여 사실상 영구 점유 |

### 호스트 관리 규칙

| 규칙 | 설명 |
|------|------|
| 정책 개별 설정 | 선점 TTL 정책은 숙소별·객실별로 독립 설정 가능 |
| 다중 날짜 차단 | 호스트는 달력에서 여러 날짜를 한 번에 선택해 일괄 차단 가능 |
| 차단 유형 구분 | 차단 사유를 BLOCKED(차단), MAINTENANCE(점검), OWNER_USE(호스트 사용)로 구분 |
| 확정 예약 차단 불가 | 고객이 결제 완료한 날짜는 호스트가 차단/해제할 수 없음 |
| 운영 정보 비공개 | 예약 건수, 차단 일수 등 운영 통계는 호스트 관리 화면에서만 표시 |

---

## 예약 상태 흐름

```
                  예약 요청
                      │
                      ▼
              ┌─────────────┐
              │  REQUESTED  │ ◄── Redis 선점 완료, 결제 대기
              │  (선점 중)   │     expiresAt 만료 시간 포함
              └──────┬──────┘
                     │
           ┌─────────┴─────────┐
           │                   │
           ▼                   ▼
    ┌────────────┐      ┌──────────┐
    │ CONFIRMED  │      │ EXPIRED  │ ◄── TTL 만료 자동 전이
    │ (결제 완료) │      │ (선점 만료)│
    └──────┬─────┘      └──────────┘
           │
     ┌─────┴──────┐
     │            │
     ▼            ▼
┌──────────┐ ┌───────────┐
│CHECKED_IN│ │ CANCELLED │ ◄── REQUESTED / CONFIRMED 에서 취소 가능
│(체크인)  │ │  (취소됨)  │     (단, CHECKED_IN 이후 취소 불가)
└────┬─────┘ └───────────┘
     │
     ▼
┌───────────┐
│CHECKED_OUT│
│(체크아웃) │
└───────────┘
```

---

## 선점 정책 (PreemptionPolicy)

결제 완료 전까지 임시로 날짜를 점유하는 TTL을 체크인 리드타임에 따라 차등 적용합니다.

### 기본값

| 정책 항목 | 기본값 | 설명 |
|-----------|--------|------|
| `shortLeadTimeDays` | 3일 | 단기 리드타임 기준 |
| `shortLeadTimeTtlMinutes` | 20분 | 3일 이내 체크인 → 짧은 TTL (공실 손실 최소화) |
| `longLeadTimeDays` | 30일 | 장기 리드타임 기준 |
| `longLeadTimeTtlMinutes` | 120분 | 30일 이상 체크인 → 긴 TTL (결제 여유 확보) |
| `defaultTtlMinutes` | 60분 | 중간 리드타임 기본값 |

### TTL 계산 로직

```
리드타임 = 체크인 날짜 - 오늘

리드타임 ≤ shortLeadTimeDays  → shortLeadTimeTtlMinutes
리드타임 ≥ longLeadTimeDays   → longLeadTimeTtlMinutes
그 외                          → defaultTtlMinutes
```

### 정책 적용 범위

- 숙소(`Accommodation`)와 객실(`Room`) 각각 독립적으로 설정 가능
- 호스트가 숙소 등록 및 객실 추가 시 개별 설정

---

## Kafka 이벤트

### 토픽

| 토픽 | 발행 컨텍스트 | 설명 |
|------|-------------|------|
| `accommodation.events` | Accommodation | 선점, 선점 만료, 예약 확정 이벤트 |
| `booking.events` | Booking | 예약 시작, 취소, 체크인/아웃 이벤트 |
| `transaction.events` | Transaction | 결제 완료, 취소, 환불 이벤트 |

> 모든 이벤트의 파티션 키는 `bookingOrderId` — 동일 예약 관련 이벤트의 순서 보장

### 핵심 이벤트 흐름

```
[고객 결제]
     │
     ▼
TransactionCompletedEvent (transaction.events)
     │
     ▼
AccommodationTransactionEventConsumer
  ├── BookingOrder 상태 확인
  ├── REQUESTED → confirmPreemption() → ConfirmedBookingDate 저장
  └── BookingConfirmedEvent 발행 (accommodation.events)
              │
              ▼
       AccommodationEventConsumer
         └── bookingOrder.confirm() → CONFIRMED

[TTL 만료]
Redis TTL 만료
     │
     ▼
SchedulePreemptionKeyExpiredListener
  └── SchedulePreemptionExpiredEvent 발행 (accommodation.events)
              │
              ▼
       AccommodationEventConsumer
         └── bookingOrder.expire() → EXPIRED
```

---

## API 엔드포인트

### 숙소 API `/api/v1/accommodations`

| 메서드 | 경로 | 설명 | 대상 |
|--------|------|------|------|
| GET | `/` | 전체 숙소 목록 조회 | 게스트/호스트 |
| GET | `/{accommodationId}` | 숙소 상세 조회 | 게스트/호스트 |
| POST | `/` | 숙소 등록 | 호스트 |
| POST | `/{accommodationId}/rooms` | 객실 추가 | 호스트 |
| POST | `/{accommodationId}/rooms/{roomId}/schedules/block` | 단일 날짜 차단 | 호스트 |
| POST | `/{accommodationId}/rooms/{roomId}/schedules/block/bulk` | 다중 날짜 일괄 차단 | 호스트 |
| DELETE | `/{accommodationId}/rooms/{roomId}/schedules/block/{date}` | 차단 해제 | 호스트 |

### 예약 API `/api/v1/booking-orders`

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/{orderId}` | 예약 상세 조회 |
| POST | `/` | 예약 생성 (Redis 선점 포함) |
| POST | `/{orderId}/confirm` | 결제 확정 |
| POST | `/{orderId}/cancel` | 예약 취소 |
| POST | `/{orderId}/check-in` | 체크인 |
| POST | `/{orderId}/check-out` | 체크아웃 |

### 숙소 응답 구조

```json
{
  "id": 1,
  "name": "해운대 오션뷰",
  "address": "부산광역시 해운대구",
  "hostName": "김호스트",
  "preemptionPolicy": {
    "shortLeadTimeDays": 3,
    "shortLeadTimeTtlMinutes": 20,
    "longLeadTimeDays": 30,
    "longLeadTimeTtlMinutes": 120,
    "defaultTtlMinutes": 60
  },
  "rooms": [
    {
      "id": 1,
      "name": "디럭스룸",
      "capacity": 2,
      "pricePerNight": 150000,
      "currency": "KRW",
      "preemptionPolicy": { ... },
      "blockedDates": [
        { "date": "2025-08-15", "type": "MAINTENANCE", "reason": "정기 점검" }
      ],
      "bookedDates": ["2025-08-20", "2025-08-21", "2025-08-22"]
    }
  ]
}
```

### 예약 주문 응답 구조

```json
{
  "orderId": 42,
  "status": "REQUESTED",
  "guestName": "홍길동",
  "totalAmount": 300000,
  "currency": "KRW",
  "expiresAt": "2025-07-01T15:30:00",
  "createdAt": "2025-07-01T15:00:00",
  "lineItems": [
    {
      "accommodationName": "해운대 오션뷰",
      "roomName": "디럭스룸",
      "checkIn": "2025-08-20",
      "checkOut": "2025-08-23",
      "nights": 3,
      "lineTotal": 300000
    }
  ]
}
```

---

## FE 화면 구성

### 게스트 화면

| 경로 | 화면 | 설명 |
|------|------|------|
| `/` | 숙소 목록 | 전체 숙소 카드 리스트 |
| `/accommodations/:id` | 숙소 상세 | 객실 목록, 예약하기 버튼 |
| `/book/:accommodationId/:roomId` | 예약 페이지 | 달력 기반 날짜 선택, 예약자 정보 입력 |
| `/bookings` | 예약 조회 | 예약번호로 조회 |
| `/bookings/:orderId` | 예약 상세 | 상태, 결제 만료 카운트다운, 결제 확정 버튼 |

### 호스트 화면

| 경로 | 화면 | 설명 |
|------|------|------|
| `/host` | 호스트 대시보드 | 등록 숙소 목록, 예약 현황 통계 |
| `/host/accommodations/new` | 숙소 등록 | 숙소 정보 + 선점 정책 설정 |
| `/host/accommodations/:id/rooms/new` | 객실 추가 | 객실 정보 + 선점 정책 설정 |
| `/host/accommodations/:id/rooms/:roomId/block` | 예약 불가 일정 설정 | 달력에서 다중 날짜 선택 후 일괄 차단/해제 |

### 핵심 컴포넌트

| 컴포넌트 | 설명 |
|----------|------|
| `DateRangePicker` | 게스트용 체크인/체크아웃 선택 달력. 예약 불가 날짜 비활성화, 범위 내 첫 번째 예약 불가 날짜 이후 선택 차단 |
| `HostCalendar` | 호스트용 날짜 관리 달력. 차단일(빨강)/확정 예약일(주황)/선택 중(파랑) 구분, 다중 선택 지원 |
| `ExpiryCountdown` | 결제 마감까지 남은 시간 실시간 표시. 5분 미만 시 경고 스타일로 전환 |
| `BookingStatusBadge` | 예약 상태 시각적 배지 |

---

## 인프라 구성

### Docker Compose 서비스

| 서비스 | 이미지 | 포트 | 설명 |
|--------|--------|------|------|
| postgres | postgres:16-alpine | 5432 | 메인 데이터베이스 |
| redis | redis:7-alpine | 6379 | 선점 키 TTL 관리, Keyspace 알림 활성화 |
| zookeeper | confluentinc/cp-zookeeper:7.6.0 | 2181 | Kafka 코디네이터 |
| kafka | confluentinc/cp-kafka:7.6.0 | 9092 | 이벤트 브로커 |
| kafka-ui | provectuslabs/kafka-ui | 8989 | Kafka 모니터링 UI |

### Redis 설정

Redis는 `--notify-keyspace-events KEx` 옵션으로 기동합니다.

| 플래그 | 의미 |
|--------|------|
| K | Keyspace 이벤트 활성화 |
| E | Keyevent 이벤트 활성화 |
| x | Key 만료(TTL expired) 이벤트 활성화 |

선점 키 TTL이 만료되면 Redis가 만료 이벤트를 발행하고,  
`SchedulePreemptionKeyExpiredListener`가 이를 수신해 `SchedulePreemptionExpiredEvent`를 Kafka로 발행합니다.

### 데이터베이스 스키마 (주요 테이블)

| 테이블 | 설명 |
|--------|------|
| `accommodation` | 숙소 정보 + 선점 정책 컬럼 |
| `room` | 객실 정보 + 선점 정책 컬럼 |
| `room_schedule` | 호스트 차단 일정 (date, type, reason) |
| `confirmed_booking_date` | 확정 예약 날짜 (unique: room_id + reserved_date) |
| `booking_order` | 예약 주문 (status, expires_at, guest_info) |
| `booking_order_line_item` | 예약 항목 (숙소/객실 스냅샷, 날짜, 금액) |
| `transaction` | 결제 정보 |
| `transaction_detail` | 결제 상세 내역 |

---

## 실행 방법

### 1. 미들웨어 기동

```bash
docker-compose up -d
```

### 2. 백엔드 실행

```bash
cd accommodation-booking-service
./gradlew bootRun --args='--spring.profiles.active=local'
```

- 기본 포트: `8080`
- 로컬 프로파일: PostgreSQL + Redis + Kafka 연결

### 3. 프론트엔드 실행

```bash
cd accommodation-booking-web
nvm use 20
npm install
npm run dev
```

- 기본 포트: `5173`
- API 프록시: `/api` → `http://localhost:8080/api`

### 4. 접속

| 서비스 | URL |
|--------|-----|
| FE (게스트) | http://localhost:5173 |
| FE (호스트) | http://localhost:5173/host |
| BE API | http://localhost:8080/api/v1 |
| Kafka UI | http://localhost:8989 |
