import { TimeSlot } from './TimeSlot'

// Domain Service: 중복 예약 확인
// Reservation 하나만으로는 판단 불가 → 여러 Reservation을 조회해야 하므로 도메인 서비스로 분리
export interface ReservationOverlapChecker {
  hasOverlap(roomId: string, timeSlot: TimeSlot): Promise<boolean>
}
