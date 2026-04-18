# 사용자 행동 흐름 (User Action Flow)

> 기반 설계: `step1_requirements/ddd_analysis.md`, `step1_requirements/requirement_card.md`
> 도메인: 예약 컨텍스트 / 숙소 컨텍스트 / 결제 컨텍스트 (Transaction)

---

## 1. 전체 흐름 (Flowchart)

```mermaid
flowchart TD
    START([고객: 숙소 및 일정 선택])
    START --> CLICK[고객: 예약하기 클릭]

    %% 선점 단계
    CLICK --> PREEMPT_CHECK{숙소 컨텍스트\nRedis 키 존재 확인}
    PREEMPT_CHECK -- 이미 선점됨 --> FAIL_PREEMPT([이미 예약 중인 일정 안내\n예약 불가])
    PREEMPT_CHECK -- 선점 가능 --> PREEMPT_CREATE[숙소 컨텍스트\nRedis 키 생성 - SchedulePreempted]

    %% 예약 데이터 생성
    PREEMPT_CREATE --> BOOKING_INIT[예약 컨텍스트\n예약 데이터 생성 - BookingInitiated]
    BOOKING_INIT --> GUEST_INPUT([고객: 예약자 정보 입력\n결제 금액 확인])

    %% TTL 만료 경로
    PREEMPT_CREATE -. TTL 만료 .-> TTL_EXPIRE[숙소 컨텍스트\nSchedulePreemptionExpired 발행]
    TTL_EXPIRE --> PREEMPT_RELEASE([선점 자동 해제\n다른 고객 예약 가능])

    %% 결제 단계
    GUEST_INPUT --> PAY_REQUEST([고객: 결제 요청])
    PAY_REQUEST --> PAY_PROCESS{결제 컨텍스트\nTransaction 결제 처리}

    %% 결제 실패 경로
    PAY_PROCESS -- 결제 실패 --> PAY_FAIL[결제 컨텍스트\nTransactionFailed 발행]
    PAY_FAIL --> REDIS_DEL_FAIL[숙소 컨텍스트\nRedis 키 삭제 - 선점 해제]
    REDIS_DEL_FAIL --> FAIL_PAY([결제 실패 안내\n예약 불가])

    %% 결제 성공 경로
    PAY_PROCESS -- 결제 성공 --> PAY_DONE[결제 컨텍스트\nTransaction 상태: Paid\nTransactionDetail 최초 row 생성\nTransactionCompleted 발행 via Kafka]
    PAY_DONE --> SCHEDULE_CONFIRM[숙소 컨텍스트\n일정 예약완료 상태 생성\nRedis 키 삭제]
    SCHEDULE_CONFIRM --> BOOKING_CONFIRMED_EVENT[숙소 컨텍스트\nBookingConfirmed 발행]
    BOOKING_CONFIRMED_EVENT --> BOOKING_STATE[예약 컨텍스트\n예약 상태 확정됨으로 변경]
    BOOKING_STATE --> CONFIRMED([예약 확정 완료])

    %% 체크인/체크아웃
    CONFIRMED --> CHECKIN_REQ([호스트: 체크인 처리 요청])
    CHECKIN_REQ --> CHECKIN[예약 컨텍스트\nCheckInRecorded 발행]
    CHECKIN --> CHECKOUT_REQ([호스트: 체크아웃 처리 요청])
    CHECKOUT_REQ --> CHECKOUT[예약 컨텍스트\nCheckOutRecorded 발행]
    CHECKOUT --> DONE([숙박 완료])

    %% 취소 흐름
    CONFIRMED --> CANCEL_REQ([고객: 취소 요청])
    CANCEL_REQ --> CANCEL_CHECK{예약 컨텍스트\n취소 가능 여부 확인}
    CANCEL_CHECK -- 체크인 이후 취소 시도 --> CANCEL_BLOCK([취소 불가\n불변조건 위반])
    CANCEL_CHECK -- 부분 취소 가능 --> PARTIAL_CANCEL[결제 컨텍스트\nTransactionDetail 환불 row 추가\nTransaction 상태: PartialCancelled\nPartialRefundProcessed 발행]
    PARTIAL_CANCEL --> PARTIAL_DONE([부분 취소 완료\n예약 DateRange 조정])

    CANCEL_CHECK -- 전체 취소 --> FULL_CANCEL_CHECK{결제 컨텍스트\n잔여 결제금액 확인}
    FULL_CANCEL_CHECK -- 잔여 금액 = 0 --> FULLY_CANCELLED[결제 컨텍스트\nTransaction 상태: FullyCancelled\nTransactionFullyRefunded 발행]
    FULL_CANCEL_CHECK -- 잔여 금액 > 0 --> PARTIAL_CANCEL
    FULLY_CANCELLED --> SCHEDULE_RESTORE[숙소 컨텍스트\n일정 선택가능 상태 복원]
    FULLY_CANCELLED --> BOOKING_CANCELLED[예약 컨텍스트\n예약 상태: 취소됨\nBookingCancelled 발행]
    SCHEDULE_RESTORE & BOOKING_CANCELLED --> CANCEL_DONE([전체 취소 완료])

    %% 스타일: 도메인별 색상
    style PREEMPT_CHECK fill:#dbeafe,stroke:#3b82f6
    style PREEMPT_CREATE fill:#dbeafe,stroke:#3b82f6
    style TTL_EXPIRE fill:#dbeafe,stroke:#3b82f6
    style SCHEDULE_CONFIRM fill:#dbeafe,stroke:#3b82f6
    style BOOKING_CONFIRMED_EVENT fill:#dbeafe,stroke:#3b82f6
    style SCHEDULE_RESTORE fill:#dbeafe,stroke:#3b82f6
    style REDIS_DEL_FAIL fill:#dbeafe,stroke:#3b82f6

    style BOOKING_INIT fill:#dcfce7,stroke:#22c55e
    style BOOKING_STATE fill:#dcfce7,stroke:#22c55e
    style CHECKIN fill:#dcfce7,stroke:#22c55e
    style CHECKOUT fill:#dcfce7,stroke:#22c55e
    style CANCEL_CHECK fill:#dcfce7,stroke:#22c55e
    style BOOKING_CANCELLED fill:#dcfce7,stroke:#22c55e

    style PAY_PROCESS fill:#fef9c3,stroke:#eab308
    style PAY_FAIL fill:#fef9c3,stroke:#eab308
    style PAY_DONE fill:#fef9c3,stroke:#eab308
    style PARTIAL_CANCEL fill:#fef9c3,stroke:#eab308
    style FULL_CANCEL_CHECK fill:#fef9c3,stroke:#eab308
    style FULLY_CANCELLED fill:#fef9c3,stroke:#eab308
```

---

## 2. 컨텍스트 간 이벤트 흐름 (Sequence Diagram)

```mermaid
sequenceDiagram
    actor 고객
    actor 호스트
    participant 예약컨텍스트 as 예약 컨텍스트
    participant 숙소컨텍스트 as 숙소 컨텍스트
    participant 결제컨텍스트 as 결제 컨텍스트 (Transaction)
    participant Kafka

    %% 선점
    고객->>숙소컨텍스트: 예약하기 클릭 (숙소ID + DateRange)
    숙소컨텍스트->>숙소컨텍스트: Redis 키 존재 확인 + 생성 (원자적)
    alt 이미 선점됨
        숙소컨텍스트-->>고객: 이미 예약 중인 일정 안내
    else 선점 성공
        숙소컨텍스트-->>예약컨텍스트: SchedulePreempted
        예약컨텍스트->>예약컨텍스트: 예약 데이터 생성 (BookingInitiated)
        예약컨텍스트-->>고객: 예약 정보 입력 화면 제공
    end

    %% 결제
    고객->>결제컨텍스트: 결제 요청
    결제컨텍스트->>결제컨텍스트: Transaction 결제 처리
    alt 결제 실패
        결제컨텍스트->>숙소컨텍스트: TransactionFailed (선점 해제 요청)
        숙소컨텍스트->>숙소컨텍스트: Redis 키 삭제
        숙소컨텍스트-->>고객: 결제 실패 / 선점 해제 안내
    else 결제 성공
        결제컨텍스트->>결제컨텍스트: Transaction 상태 → Paid
        결제컨텍스트->>결제컨텍스트: TransactionDetail 최초 row 생성 (LedgerInfo 포함)
        결제컨텍스트->>Kafka: TransactionCompleted 발행
        Kafka->>숙소컨텍스트: TransactionCompleted 수신
        숙소컨텍스트->>숙소컨텍스트: 일정 예약완료 상태 생성 + Redis 키 삭제
        숙소컨텍스트->>Kafka: BookingConfirmed 발행
        Kafka->>예약컨텍스트: BookingConfirmed 수신
        예약컨텍스트->>예약컨텍스트: 예약 상태 → 확정됨
        예약컨텍스트-->>고객: 예약 확정 알림
    end

    %% TTL 만료 (비동기)
    Note over 숙소컨텍스트: Redis TTL 만료 시
    숙소컨텍스트->>숙소컨텍스트: SchedulePreemptionExpired 발행 + 선점 자동 해제

    %% 체크인/체크아웃
    호스트->>예약컨텍스트: 체크인 처리
    예약컨텍스트->>예약컨텍스트: CheckInRecorded 발행
    호스트->>예약컨텍스트: 체크아웃 처리
    예약컨텍스트->>예약컨텍스트: CheckOutRecorded 발행

    %% 부분 취소
    고객->>예약컨텍스트: 부분 취소 요청
    alt 체크인 이후 취소 시도
        예약컨텍스트-->>고객: 취소 불가 (불변조건)
    else 부분 취소 가능
        예약컨텍스트->>결제컨텍스트: BookingPartiallyCancelled 전달
        결제컨텍스트->>결제컨텍스트: TransactionDetail 환불 row 추가 (RefundAmount, RefundStatus)
        결제컨텍스트->>결제컨텍스트: Transaction 상태 → PartialCancelled
        결제컨텍스트->>Kafka: PartialRefundProcessed 발행
        Kafka->>예약컨텍스트: PartialRefundProcessed 수신
        예약컨텍스트->>예약컨텍스트: 예약 DateRange 조정
        예약컨텍스트-->>고객: 부분 취소 완료 알림
    end

    %% 전체 취소
    고객->>예약컨텍스트: 전체 취소 요청
    예약컨텍스트->>결제컨텍스트: BookingCancelled 전달
    결제컨텍스트->>결제컨텍스트: 잔여 결제금액 확인
    결제컨텍스트->>결제컨텍스트: Transaction 상태 → FullyCancelled
    결제컨텍스트->>Kafka: TransactionFullyRefunded 발행
    Kafka->>숙소컨텍스트: TransactionFullyRefunded 수신
    숙소컨텍스트->>숙소컨텍스트: 일정 선택가능 상태 복원
    Kafka->>예약컨텍스트: TransactionFullyRefunded 수신
    예약컨텍스트->>예약컨텍스트: BookingCancelled 발행 + 예약 상태 → 취소됨
    예약컨텍스트-->>고객: 전체 취소 및 환불 완료 알림
```

---

## 3. Transaction 상태 전이 다이어그램

```mermaid
stateDiagram-v2
    [*] --> Paid : 결제 성공\nTransactionCompleted\nTransactionDetail row 생성

    Paid --> PartialCancelled : 부분 취소 요청\nPartialRefundProcessed\nTransactionDetail 환불 row 추가

    PartialCancelled --> PartialCancelled : 추가 부분 취소\nPartialRefundProcessed\nTransactionDetail 환불 row 누적

    PartialCancelled --> FullyCancelled : 잔여금액 = 0\nTransactionFullyRefunded

    Paid --> FullyCancelled : 전액 취소\nTransactionFullyRefunded

    FullyCancelled --> [*]

    note right of PartialCancelled
        TransactionDetail은 append-only
        취소마다 환불 row 추가
        row 수정 불가 (불변조건)
    end note
```

---

## 4. 도메인 이벤트 발생 타임라인

```mermaid
timeline
    title 예약 확정 Happy Path 이벤트 흐름
    section 선점 단계
        고객 예약하기 클릭 : SchedulePreempted
                           : BookingInitiated
    section 결제 단계
        고객 결제 요청     : TransactionCompleted
                           : TransactionDetail 최초 row 생성
    section 확정 단계
        숙소 일정 확정     : BookingConfirmed
    section 체크인/아웃
        호스트 체크인      : CheckInRecorded
        호스트 체크아웃    : CheckOutRecorded
```

---

## 5. 도메인별 책임 요약

| 색상 | 컨텍스트 | 주요 책임 |
|------|---------|---------|
| 파랑 | 숙소 컨텍스트 | 선점 생성/해제, 일정 상태 관리, Redis 키 생명주기 |
| 초록 | 예약 컨텍스트 | 예약 상태 전이, 불변조건 강제, 체크인/아웃 기록 |
| 노랑 | 결제 컨텍스트 (Transaction) | Transaction/TransactionDetail 원장 관리, 부분·전체 취소, 이벤트 발행 (Kafka) |