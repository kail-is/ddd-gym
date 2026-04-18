// Value Object: 시간대 (시작시간 ~ 종료시간)
export class TimeSlot {
  constructor(
    readonly startAt: Date,
    readonly endAt: Date,
  ) {
    if (startAt >= endAt) {
      throw new Error('시작 시간은 종료 시간보다 이전이어야 합니다.')
    }
  }

  overlaps(other: TimeSlot): boolean {
    return this.startAt < other.endAt && this.endAt > other.startAt
  }

  isPast(): boolean {
    return this.startAt < new Date()
  }

  equals(other: TimeSlot): boolean {
    return (
      this.startAt.getTime() === other.startAt.getTime() &&
      this.endAt.getTime() === other.endAt.getTime()
    )
  }
}
