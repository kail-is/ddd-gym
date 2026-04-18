package booking

/**
 * 예약 상태
 */
enum class BookingStatus {
    REQUESTED,    // 요청됨
    CONFIRMED,    // 확정됨
    REJECTED,     // 거절됨
    CANCELLED,    // 취소됨
    CHECKED_IN,   // 체크인
    CHECKED_OUT   // 체크아웃
}
