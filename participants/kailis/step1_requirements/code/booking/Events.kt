package booking

import java.time.LocalDateTime

/**
 * 도메인 이벤트 마커 인터페이스
 */
sealed interface BookingEvent {
    val bookingId: BookingId
    val occurredAt: LocalDateTime
}

/** 예약 요청됨 */
data class BookingRequested(
    override val bookingId: BookingId,
    val guestId: GuestId,
    val accommodationId: AccommodationId,
    val dateRange: DateRange,
    override val occurredAt: LocalDateTime
) : BookingEvent

/** 예약 확정됨 */
data class BookingConfirmed(
    override val bookingId: BookingId,
    override val occurredAt: LocalDateTime
) : BookingEvent

/** 예약 거절됨 */
data class BookingRejected(
    override val bookingId: BookingId,
    override val occurredAt: LocalDateTime
) : BookingEvent

/** 예약 취소됨 */
data class BookingCancelled(
    override val bookingId: BookingId,
    val reason: CancellationReason,
    override val occurredAt: LocalDateTime
) : BookingEvent

/** 체크인 완료 */
data class CheckedIn(
    override val bookingId: BookingId,
    override val occurredAt: LocalDateTime
) : BookingEvent

/** 체크아웃 완료 */
data class CheckedOut(
    override val bookingId: BookingId,
    override val occurredAt: LocalDateTime
) : BookingEvent
