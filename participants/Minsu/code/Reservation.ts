import { DomainEvent } from './DomainEvent'
import { PriceSnapshot } from './PriceSnapshot'
import { ReservationOverlapChecker } from './ReservationOverlapChecker'
import { ReservationStatus } from './ReservationStatus'
import {
  CustomerReservationCancelled,
  HostReservationCancelled,
  ReservationCheckedIn,
  ReservationCheckedOut,
  ReservationConfirmed,
} from './ReservationEvents'
import { TimeSlot } from './TimeSlot'

interface ReservationProps {
  id: string
  roomId: string       // 숙소 관리 컨텍스트 참조값 (VO)
  customerId: string   // 고객 테이블 참조값 (VO)
  timeSlot: TimeSlot
  guestCount: number
  priceSnapshot: PriceSnapshot  // 예약 생성 시점 금액 고정 (불변)
  requestedAt: Date
}

// Aggregate Root: 예약 관리 컨텍스트의 핵심 애그리게잇
// 불변조건을 스스로 지키며, 상태 변화를 도메인 이벤트로 발행
export class Reservation {
  private readonly _id: string
  private readonly _roomId: string
  private readonly _customerId: string
  private readonly _timeSlot: TimeSlot
  private readonly _guestCount: number
  private readonly _priceSnapshot: PriceSnapshot
  private readonly _requestedAt: Date
  private _status: ReservationStatus
  private _domainEvents: DomainEvent[] = []

  private constructor(props: ReservationProps) {
    this._id = props.id
    this._roomId = props.roomId
    this._customerId = props.customerId
    this._timeSlot = props.timeSlot
    this._guestCount = props.guestCount
    this._priceSnapshot = props.priceSnapshot
    this._requestedAt = props.requestedAt
    this._status = ReservationStatus.REQUESTED
  }

  // 예약 생성 (요청됨 상태로 시작)
  // 불변조건: 중복 시간대 예약 불가 → ReservationOverlapChecker로 검증
  static async create(
    props: Omit<ReservationProps, 'requestedAt'>,
    overlapChecker: ReservationOverlapChecker,
  ): Promise<Reservation> {
    if (props.timeSlot.isPast()) {
      throw new Error('이미 지나간 시간대는 예약할 수 없습니다.')
    }

    const hasOverlap = await overlapChecker.hasOverlap(props.roomId, props.timeSlot)
    if (hasOverlap) {
      throw new Error('해당 시간대에 이미 예약이 존재합니다.')
    }

    return new Reservation({ ...props, requestedAt: new Date() })
  }

  // 확정 (요청됨 → 확정됨)
  // 스케줄링 시스템이 3시간 후 호출
  // 불변조건: 취소된 예약은 재확정 불가
  confirm(): void {
    if (this._status !== ReservationStatus.REQUESTED) {
      throw new Error('요청됨 상태에서만 확정할 수 있습니다.')
    }
    this._status = ReservationStatus.CONFIRMED
    this._domainEvents.push(new ReservationConfirmed(this._id))
  }

  // 고객 취소 (요청됨/확정됨 → 취소됨)
  // 불변조건: 체크인 이후 취소 불가
  // 정책: 확정됨 상태에서 취소 시 비용 발생 (이벤트로 정산 시스템에 위임)
  cancelByCustomer(): void {
    if (
      this._status === ReservationStatus.CHECKED_IN ||
      this._status === ReservationStatus.CHECKED_OUT
    ) {
      throw new Error('체크인 이후에는 취소할 수 없습니다.')
    }
    if (this._status === ReservationStatus.CANCELLED) {
      throw new Error('이미 취소된 예약입니다.')
    }
    this._status = ReservationStatus.CANCELLED
    this._domainEvents.push(
      new CustomerReservationCancelled(this._id, this._customerId),
    )
  }

  // 호스트 취소 (요청됨/확정됨 → 취소됨)
  // 불변조건: 체크인 이후 취소 불가
  // 정책: 확정됨 상태에서 취소 시 고객 보상 발생 (이벤트로 정산 시스템에 위임)
  cancelByHost(hostId: string): void {
    if (
      this._status === ReservationStatus.CHECKED_IN ||
      this._status === ReservationStatus.CHECKED_OUT
    ) {
      throw new Error('체크인 이후에는 취소할 수 없습니다.')
    }
    if (this._status === ReservationStatus.CANCELLED) {
      throw new Error('이미 취소된 예약입니다.')
    }
    this._status = ReservationStatus.CANCELLED
    this._domainEvents.push(new HostReservationCancelled(this._id, hostId))
  }

  // 체크인 (확정됨 → 체크인)
  checkIn(): void {
    if (this._status !== ReservationStatus.CONFIRMED) {
      throw new Error('확정된 예약만 체크인할 수 있습니다.')
    }
    this._status = ReservationStatus.CHECKED_IN
    this._domainEvents.push(new ReservationCheckedIn(this._id))
  }

  // 체크아웃 (체크인 → 체크아웃)
  // 불변조건: 체크인 전 체크아웃 불가
  checkOut(): void {
    if (this._status !== ReservationStatus.CHECKED_IN) {
      throw new Error('체크인 이후에만 체크아웃할 수 있습니다.')
    }
    this._status = ReservationStatus.CHECKED_OUT
    this._domainEvents.push(new ReservationCheckedOut(this._id))
  }

  // 발행된 도메인 이벤트 수집 후 초기화
  // 이벤트 기반으로 정산 시스템에 전달
  pullDomainEvents(): DomainEvent[] {
    const events = [...this._domainEvents]
    this._domainEvents = []
    return events
  }

  get id() { return this._id }
  get roomId() { return this._roomId }
  get customerId() { return this._customerId }
  get timeSlot() { return this._timeSlot }
  get guestCount() { return this._guestCount }
  get priceSnapshot() { return this._priceSnapshot }
  get status() { return this._status }
  get requestedAt() { return this._requestedAt }
}
