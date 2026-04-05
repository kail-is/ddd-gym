/**
 * DDD Gym - 숙박 예약 도메인 예시 (TypeScript)
 *
 * 이 코드는 완전한 구현이 아닌 설계 의도를 보여주는 스케치입니다.
 * 클래스 구조와 불변조건 표현에 집중하세요.
 */

// ============================================
// Value Objects
// ============================================

/** 예약 기간 (Value Object) */
class StayPeriod {
  constructor(
    readonly checkInDate: Date,
    readonly checkOutDate: Date
  ) {
    if (checkOutDate <= checkInDate) {
      throw new Error("체크아웃 날짜는 체크인 날짜 이후여야 합니다");
    }
  }

  overlaps(other: StayPeriod): boolean {
    return !(
      this.checkOutDate <= other.checkInDate ||
      this.checkInDate >= other.checkOutDate
    );
  }

  get nights(): number {
    const diffTime = this.checkOutDate.getTime() - this.checkInDate.getTime();
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  }

  equals(other: StayPeriod): boolean {
    return (
      this.checkInDate.getTime() === other.checkInDate.getTime() &&
      this.checkOutDate.getTime() === other.checkOutDate.getTime()
    );
  }
}

/** 인원 수 (Value Object) */
class GuestCount {
  constructor(
    readonly adults: number,
    readonly children: number = 0
  ) {
    if (adults < 1) {
      throw new Error("최소 1명의 성인이 필요합니다");
    }
    if (children < 0) {
      throw new Error("어린이 수는 0 이상이어야 합니다");
    }
  }

  get total(): number {
    return this.adults + this.children;
  }

  equals(other: GuestCount): boolean {
    return this.adults === other.adults && this.children === other.children;
  }
}

/** 예약 ID (Value Object) */
class BookingId {
  constructor(readonly value: string) {
    if (!value || value.trim() === "") {
      throw new Error("예약 ID는 비어있을 수 없습니다");
    }
  }

  equals(other: BookingId): boolean {
    return this.value === other.value;
  }

  toString(): string {
    return this.value;
  }
}

// ============================================
// Entity & Aggregate Root
// ============================================

/** 예약 상태 */
enum BookingStatus {
  REQUESTED = "REQUESTED",     // 요청됨
  CONFIRMED = "CONFIRMED",     // 확정됨
  CANCELLED = "CANCELLED",     // 취소됨
  CHECKED_IN = "CHECKED_IN",   // 체크인
  CHECKED_OUT = "CHECKED_OUT"  // 체크아웃
}

/** Domain Event 타입 */
type DomainEvent =
  | BookingRequested
  | BookingConfirmed
  | BookingCancelled
  | CheckedIn
  | CheckedOut;

interface BookingRequested {
  type: "BookingRequested";
  bookingId: BookingId;
  accommodationId: string;
  period: StayPeriod;
}

interface BookingConfirmed {
  type: "BookingConfirmed";
  bookingId: BookingId;
}

interface BookingCancelled {
  type: "BookingCancelled";
  bookingId: BookingId;
  reason: string;
}

interface CheckedIn {
  type: "CheckedIn";
  bookingId: BookingId;
}

interface CheckedOut {
  type: "CheckedOut";
  bookingId: BookingId;
}

/**
 * 예약 (Aggregate Root)
 *
 * 불변조건:
 * - 취소된 예약은 다시 확정할 수 없다
 * - 체크인 이후에는 취소할 수 없다
 * - 체크인 없이 체크아웃할 수 없다
 */
class Booking {
  private _status: BookingStatus;
  private _events: DomainEvent[] = [];

  private constructor(
    readonly id: BookingId,
    readonly accommodationId: string,
    readonly guestId: string,
    readonly period: StayPeriod,
    readonly guestCount: GuestCount
  ) {
    this._status = BookingStatus.REQUESTED;
  }

  static request(
    id: BookingId,
    accommodationId: string,
    guestId: string,
    period: StayPeriod,
    guestCount: GuestCount
  ): Booking {
    const booking = new Booking(id, accommodationId, guestId, period, guestCount);
    booking._events.push({
      type: "BookingRequested",
      bookingId: id,
      accommodationId,
      period
    });
    return booking;
  }

  get status(): BookingStatus {
    return this._status;
  }

  get events(): readonly DomainEvent[] {
    return [...this._events];
  }

  /** 예약 확정 */
  confirm(): void {
    // 불변조건: 취소된 예약은 재확정 불가
    if (this._status === BookingStatus.CANCELLED) {
      throw new Error("취소된 예약은 다시 확정할 수 없습니다");
    }
    if (this._status !== BookingStatus.REQUESTED) {
      throw new Error("요청 상태의 예약만 확정할 수 있습니다");
    }

    this._status = BookingStatus.CONFIRMED;
    this._events.push({
      type: "BookingConfirmed",
      bookingId: this.id
    });
  }

  /** 예약 취소 */
  cancel(reason: string): void {
    // 불변조건: 체크인 이후 취소 불가
    if (
      this._status === BookingStatus.CHECKED_IN ||
      this._status === BookingStatus.CHECKED_OUT
    ) {
      throw new Error("체크인 이후에는 취소할 수 없습니다");
    }
    if (this._status === BookingStatus.CANCELLED) {
      throw new Error("이미 취소된 예약입니다");
    }

    this._status = BookingStatus.CANCELLED;
    this._events.push({
      type: "BookingCancelled",
      bookingId: this.id,
      reason
    });
  }

  /** 체크인 */
  checkIn(): void {
    if (this._status !== BookingStatus.CONFIRMED) {
      throw new Error("확정된 예약만 체크인할 수 있습니다");
    }

    this._status = BookingStatus.CHECKED_IN;
    this._events.push({
      type: "CheckedIn",
      bookingId: this.id
    });
  }

  /** 체크아웃 */
  checkOut(): void {
    // 불변조건: 체크인 없이 체크아웃 불가
    if (this._status !== BookingStatus.CHECKED_IN) {
      throw new Error("체크인된 예약만 체크아웃할 수 있습니다");
    }

    this._status = BookingStatus.CHECKED_OUT;
    this._events.push({
      type: "CheckedOut",
      bookingId: this.id
    });
  }

  clearEvents(): void {
    this._events = [];
  }
}

// ============================================
// 사용 예시
// ============================================

function main() {
  const booking = Booking.request(
    new BookingId("BK-001"),
    "ACC-001",
    "GUEST-001",
    new StayPeriod(
      new Date("2024-07-20"),
      new Date("2024-07-22")
    ),
    new GuestCount(2, 1)
  );

  // 예약 확정
  booking.confirm();

  // 체크인
  booking.checkIn();

  // 체크아웃
  booking.checkOut();

  // 발생한 이벤트 확인
  console.log("발생한 이벤트:");
  booking.events.forEach((event) => console.log(event));
}

// Export for module usage
export { Booking, BookingId, BookingStatus, StayPeriod, GuestCount };
export type { DomainEvent };
