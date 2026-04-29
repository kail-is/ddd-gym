# 요약 노트

## 요구사항
- 링크/파일: `step1_requirements/requirement_card.md`, `step1_requirements/ddd_analysis.md`
- 요약: 숙박 예약의 확정, 취소, 체크인/체크아웃 흐름과 결제/HOLD를 포함한 확정 처리 규칙을 설계했다.

## 설계 요약
- 경계는 `Booking`, `RoomAvailability`, `CheckIn`, `Payment`로 나눴다.
- `Booking`은 최종 결과 상태를 관리하고, `RoomAvailability`는 동일 기간 점유와 `HOLD` 유지 여부를 판단하며, `Payment`는 결제 진행/재시도를 관리한다.
- `RoomReserved`는 예약 확정의 전제 사실이고, 모든 조건 충족 뒤 `BookingConfirmed`가 최종 확정 사실로 발행된다.

## 핵심 결정 2개
1. `Booking.status`는 진행 세부 단계가 아니라 최종 결과 상태만 표현한다.
2. `RoomAvailability`와 `Payment`는 별도 트랜잭션으로 두고, 확정 처리 실패 시 Application Service/Saga가 보상 책임을 가진다.

## 버린 대안
- `Booking` 하나의 상태값으로 결제 재시도, `HOLD` 연장, 보상 대기까지 모두 표현하는 방식은 의미가 너무 넓어져 해석 충돌을 만든다고 판단했다.

## 시나리오 결과
- 정상 시나리오: `HOLD` 유지 가능, 결제 성공, 점유 성공 이후 `Booking.status = CONFIRMED`, `BookingConfirmed` 발행
- 예외/경계 시나리오: 중복 점유 실패, `HOLD` 만료 직후 확정 실패, `HOLD` 만료 시점 결제 진입

## 테스트 결과
- 정상 케이스: 점유 성공 시 예약 확정
- 예외/경계/위험 중 부족한 영역: 사용자 취소와 확정 실패의 종료 의미 비교
- 추가 보강: `RoomReserved` 후 `BookingConfirmed` 실패 시 `RoomReleased`, 결제 취소/환불 요청, `BookingCancelled(reason=CONFIRMATION_FAILED)`까지 이어지는 보상 흐름 테스트를 추가했다

## AI 질문으로 수정된 내용
- 질문: `RoomReserved`는 `BookingConfirmed`의 결과인가요, 전제인가요?
- 수정: `RoomReserved`를 선행 사실로, `BookingConfirmed`를 최종 확정 사실로 구분했다.

- 질문: "예약 확정 시점" 한 줄로 트랜잭션 경계를 설명할 수 있나요?
- 수정: Booking 내부 트랜잭션, 외부/별도 트랜잭션, 보상 책임 주체(Application Service/Saga)를 명시했다.

- 질문: `REQUESTED`가 여러 진행 상황을 모두 담아도 되나요?
- 수정: `Booking.status`는 최종 결과만 표현하고, 진행 중 의미는 `Payment`와 `HOLD` 정책이 나눠 갖도록 정리했다.

- 질문: `BookingCancelled` 하나에 여러 종료 이유를 담는 방식이 충분한가요?
- 수정: 종료 이유를 별도 상태로 모두 승격하면 복잡도가 커지므로, `Booking`은 최종 상태만 가지고 종료 원인은 `reason`으로 구분하는 방향을 유지했다.

## 고민 포인트
- `REQUESTED`가 진행 중 상태를 충분히 설명하지 못하므로, 조회 모델이나 운영 상태를 별도로 둬야 하는가?

## 파괴적 요구사항 제안
- **요구사항**: 결제 승인 후 일정 시간 내 객실 점유 확정이 실패하면 자동으로 대체 객실을 제안한다.
- **왜 깨지나**: 현재 설계는 실패 시 취소/보상 흐름에 집중되어 있고, 실패 이후 대체 확정 흐름을 직접 다루지 않는다.
- **어디가 먼저 깨지나**: `Payment` 보상 정책, `RoomAvailability` 점유 실패 후 처리, `Booking`의 최종 상태 정의

## 체크리스트
- [x] 요구사항~질문 루프 문서 링크 첨부.
- [x] 테스트 유형별 상태 기록.
- [x] 미완료 항목은 TODO 표시.
