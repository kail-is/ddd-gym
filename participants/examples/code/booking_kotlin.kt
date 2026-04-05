/**
 * DDD Gym - 숙박 예약 도메인 예시 (Kotlin)
 *
 * 이 코드는 완전한 구현이 아닌 설계 의도를 보여주는 스케치입니다.
 * 클래스 구조와 불변조건 표현에 집중하세요.
 */

// ============================================
// Value Objects
// ============================================

/** 예약 기간 (Value Object) */
data class StayPeriod(
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate
) {
    init {
        require(checkOutDate.isAfter(checkInDate)) {
            "체크아웃 날짜는 체크인 날짜 이후여야 합니다"
        }
    }

    fun overlaps(other: StayPeriod): Boolean =
        !(checkOutDate <= other.checkInDate || checkInDate >= other.checkOutDate)

    val nights: Int get() = ChronoUnit.DAYS.between(checkInDate, checkOutDate).toInt()
}

/** 인원 수 (Value Object) */
data class GuestCount(
    val adults: Int,
    val children: Int = 0
) {
    init {
        require(adults >= 1) { "최소 1명의 성인이 필요합니다" }
        require(children >= 0) { "어린이 수는 0 이상이어야 합니다" }
    }

    val total: Int get() = adults + children
}

/** 예약 ID (Value Object) */
@JvmInline
value class BookingId(val value: String) {
    init {
        require(value.isNotBlank()) { "예약 ID는 비어있을 수 없습니다" }
    }
}

// ============================================
// Entity & Aggregate Root
// ============================================

/** 예약 상태 */
enum class BookingStatus {
    REQUESTED,   // 요청됨
    CONFIRMED,   // 확정됨
    CANCELLED,   // 취소됨
    CHECKED_IN,  // 체크인
    CHECKED_OUT  // 체크아웃
}

/**
 * 예약 (Aggregate Root)
 *
 * 불변조건:
 * - 취소된 예약은 다시 확정할 수 없다
 * - 체크인 이후에는 취소할 수 없다
 * - 체크인 없이 체크아웃할 수 없다
 */
class Booking private constructor(
    val id: BookingId,
    val accommodationId: String,
    val guestId: String,
    val period: StayPeriod,
    val guestCount: GuestCount,
    private var _status: BookingStatus,
    private val _events: MutableList<DomainEvent> = mutableListOf()
) {
    val status: BookingStatus get() = _status
    val events: List<DomainEvent> get() = _events.toList()

    companion object {
        fun request(
            id: BookingId,
            accommodationId: String,
            guestId: String,
            period: StayPeriod,
            guestCount: GuestCount
        ): Booking {
            return Booking(
                id = id,
                accommodationId = accommodationId,
                guestId = guestId,
                period = period,
                guestCount = guestCount,
                _status = BookingStatus.REQUESTED
            ).also {
                it._events.add(BookingRequested(it.id, accommodationId, period))
            }
        }
    }

    /** 예약 확정 */
    fun confirm() {
        // 불변조건: 취소된 예약은 재확정 불가
        check(status != BookingStatus.CANCELLED) {
            "취소된 예약은 다시 확정할 수 없습니다"
        }
        check(status == BookingStatus.REQUESTED) {
            "요청 상태의 예약만 확정할 수 있습니다"
        }

        _status = BookingStatus.CONFIRMED
        _events.add(BookingConfirmed(id))
    }

    /** 예약 취소 */
    fun cancel(reason: String) {
        // 불변조건: 체크인 이후 취소 불가
        check(status != BookingStatus.CHECKED_IN && status != BookingStatus.CHECKED_OUT) {
            "체크인 이후에는 취소할 수 없습니다"
        }
        check(status != BookingStatus.CANCELLED) {
            "이미 취소된 예약입니다"
        }

        _status = BookingStatus.CANCELLED
        _events.add(BookingCancelled(id, reason))
    }

    /** 체크인 */
    fun checkIn() {
        check(status == BookingStatus.CONFIRMED) {
            "확정된 예약만 체크인할 수 있습니다"
        }

        _status = BookingStatus.CHECKED_IN
        _events.add(CheckedIn(id))
    }

    /** 체크아웃 */
    fun checkOut() {
        // 불변조건: 체크인 없이 체크아웃 불가
        check(status == BookingStatus.CHECKED_IN) {
            "체크인된 예약만 체크아웃할 수 있습니다"
        }

        _status = BookingStatus.CHECKED_OUT
        _events.add(CheckedOut(id))
    }

    fun clearEvents() {
        _events.clear()
    }
}

// ============================================
// Domain Events
// ============================================

sealed interface DomainEvent

data class BookingRequested(
    val bookingId: BookingId,
    val accommodationId: String,
    val period: StayPeriod
) : DomainEvent

data class BookingConfirmed(val bookingId: BookingId) : DomainEvent

data class BookingCancelled(
    val bookingId: BookingId,
    val reason: String
) : DomainEvent

data class CheckedIn(val bookingId: BookingId) : DomainEvent

data class CheckedOut(val bookingId: BookingId) : DomainEvent

// ============================================
// 사용 예시
// ============================================

fun main() {
    val booking = Booking.request(
        id = BookingId("BK-001"),
        accommodationId = "ACC-001",
        guestId = "GUEST-001",
        period = StayPeriod(
            checkInDate = LocalDate.of(2024, 7, 20),
            checkOutDate = LocalDate.of(2024, 7, 22)
        ),
        guestCount = GuestCount(adults = 2, children = 1)
    )

    // 예약 확정
    booking.confirm()

    // 체크인
    booking.checkIn()

    // 체크아웃
    booking.checkOut()

    // 발생한 이벤트 확인
    booking.events.forEach { println(it) }
}
