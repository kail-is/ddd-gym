// Value Object: 예약 생성 시점의 금액 스냅샷
// 가격 정책(숙소 관리 컨텍스트)에서 가져온 값을 예약 시점에 고정 저장
export class PriceSnapshot {
  constructor(
    readonly amount: number,
    readonly currency: string = 'KRW',
  ) {
    if (amount < 0) {
      throw new Error('금액은 0 이상이어야 합니다.')
    }
  }

  equals(other: PriceSnapshot): boolean {
    return this.amount === other.amount && this.currency === other.currency
  }
}
