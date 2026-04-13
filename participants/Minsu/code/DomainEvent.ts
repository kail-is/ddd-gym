// 도메인 이벤트 기본 인터페이스
// 상태 변화가 일어난 사실을 과거형으로 표현
export interface DomainEvent {
  readonly eventName: string
  readonly reservationId: string
  readonly occurredAt: Date
}
