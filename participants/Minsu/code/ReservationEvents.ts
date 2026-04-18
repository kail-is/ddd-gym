import { DomainEvent } from './DomainEvent'

// 예약 확정 (요청됨 → 확정됨, 3시간 후 자동)
export class ReservationConfirmed implements DomainEvent {
  readonly eventName = 'ReservationConfirmed'
  readonly occurredAt = new Date()

  constructor(readonly reservationId: string) {}
}

// 고객 예약 취소 → 정산: 체크인 날짜 기준 취소 비용 발생 (정책)
export class CustomerReservationCancelled implements DomainEvent {
  readonly eventName = 'CustomerReservationCancelled'
  readonly occurredAt = new Date()

  constructor(
    readonly reservationId: string,
    readonly customerId: string,
  ) {}
}

// 호스트 예약 취소 → 정산: 예약일 기준 고객 보상 발생 (정책)
export class HostReservationCancelled implements DomainEvent {
  readonly eventName = 'HostReservationCancelled'
  readonly occurredAt = new Date()

  constructor(
    readonly reservationId: string,
    readonly hostId: string,
  ) {}
}

// 체크인 (확정됨 → 체크인)
export class ReservationCheckedIn implements DomainEvent {
  readonly eventName = 'ReservationCheckedIn'
  readonly occurredAt = new Date()

  constructor(readonly reservationId: string) {}
}

// 체크아웃 (체크인 → 체크아웃)
export class ReservationCheckedOut implements DomainEvent {
  readonly eventName = 'ReservationCheckedOut'
  readonly occurredAt = new Date()

  constructor(readonly reservationId: string) {}
}
