/**
 * [Value Object] 금액 정보를 담는 VO
 */
export class Money {
    constructor(readonly amount: number, readonly currency: string = 'KRW') {
        if (amount < 0) throw new Error("가격은 0원보다 적을 수 없습니다.");
    }
}

/**
 * [Value Object] 가격이 적용되는 시간 단위
 */
export class PricePerSlot {
    constructor(
        readonly hourSlot: number, // 예: 1시간 단위
        readonly price: Money
    ) {}
}

/**
 * [Aggregate Root] 숙소의 가격 정책
 */
export class PricingPolicy {
    private constructor(
        private readonly roomId: string,
        private basePrice: Money,
        private minBookingHours: number, // 최소 예약 시간 규칙
        private isAvailable: boolean
    ) {}

    // 팩토리 메서드: 새로운 정책 생성
    static create(roomId: string, basePrice: Money, minHours: number): PricingPolicy {
        if (minHours < 1) throw new Error("최소 예약 시간은 1시간 이상이어야 합니다.");
        return new PricingPolicy(roomId, basePrice, minHours, true);
    }

    /**
     * [Business Logic] 예약 요청 시 최종 금액 계산 및 규칙 검증
     * Reservation 애그리거트 생성 시 이 메서드를 통해 '금액 스냅샷'을 가져감
     */
    public calculateTotalFee(hours: number): Money {
        this.validateBookingRules(hours);

        const totalAmount = this.basePrice.amount * hours;
        return new Money(totalAmount);
    }

    /**
     * [Invariants] 비즈니스 불변 조건 검증
     */
    private validateBookingRules(hours: number): void {
        if (!this.isAvailable) {
            throw new Error("현재 이 숙소는 예약 가능한 상태가 아닙니다.");
        }
        if (hours < this.minBookingHours) {
            throw new Error(`최소 ${this.minBookingHours}시간 이상 예약해야 합니다.`);
        }
    }

    /**
     * [State Transition] 호스트가 숙소 가용 여부 변경
     */
    public updateAvailability(status: boolean): void {
        this.isAvailable = status;
    }

    // Getter
    public getRoomId(): string { return this.roomId; }
}