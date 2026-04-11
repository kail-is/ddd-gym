package booking

import java.time.LocalDateTime
import java.util.UUID

/**
 * 예약 Aggregate Root
 */
class Booking private constructor(
    val id: BookingId,
    val guestId: GuestId,
    val accommodationId: AccommodationId,
    val dateRange: DateRange,
    val guestCount: GuestCount,
    private var _status: BookingStatus,
    private var _cancellationReason: CancellationReason? = null
) {
    val status: BookingStatus get() = _status
    val cancellationReason: CancellationReason? get() = _cancellationReason

    companion object {
        fun request(
            guestId: GuestId,
            accommodationId: AccommodationId,
            dateRange: DateRange,
            guestCount: GuestCount
        ): Pair<Booking, BookingRequested> {
            val booking = Booking(
                id = BookingId(UUID.randomUUID()),
                guestId = guestId,
                accommodationId = accommodationId,
                dateRange = dateRange,
                guestCount = guestCount,
                _status = BookingStatus.REQUESTED
            )
            val event = BookingRequested(
                bookingId = booking.id,
                guestId = guestId,
                accommodationId = accommodationId,
                dateRange = dateRange,
                occurredAt = LocalDateTime.now()
            )
            return booking to event
        }
    }

    // 숙소가 확정
    fun confirm(): BookingConfirmed {
        require(_status == BookingStatus.REQUESTED) {
            "요청됨 상태에서만 확정 가능"
        }
        _status = BookingStatus.CONFIRMED
        return BookingConfirmed(id, LocalDateTime.now())
    }

    // 숙소가 거절
    fun reject(): BookingRejected {
        require(_status == BookingStatus.REQUESTED) {
            "요청됨 상태에서만 거절 가능"
        }
        _status = BookingStatus.REJECTED
        return BookingRejected(id, LocalDateTime.now())
    }

    // 취소 (고객 요청 후 숙소 확정, 또는 숙소 직접)
    fun cancel(reason: CancellationReason): BookingCancelled {
        require(_status == BookingStatus.CONFIRMED) {
            "확정됨 상태에서만 취소 가능"
        }
        _status = BookingStatus.CANCELLED
        _cancellationReason = reason
        return BookingCancelled(id, reason, LocalDateTime.now())
    }

    // 고객이 체크인
    fun checkIn(): CheckedIn {
        require(_status == BookingStatus.CONFIRMED) {
            "확정됨 상태에서만 체크인 가능"
        }
        _status = BookingStatus.CHECKED_IN
        return CheckedIn(id, LocalDateTime.now())
    }

    // 숙소가 체크아웃
    fun checkOut(): CheckedOut {
        require(_status == BookingStatus.CHECKED_IN) {
            "체크인 상태에서만 체크아웃 가능"
        }
        _status = BookingStatus.CHECKED_OUT
        return CheckedOut(id, LocalDateTime.now())
    }
}
