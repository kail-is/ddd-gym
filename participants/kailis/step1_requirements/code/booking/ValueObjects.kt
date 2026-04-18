package booking

import java.time.LocalDate
import java.util.UUID

/** 예약 ID */
@JvmInline
value class BookingId(val value: UUID)

/** 고객 ID */
@JvmInline
value class GuestId(val value: UUID)

/** 숙소 ID */
@JvmInline
value class AccommodationId(val value: UUID)

/**
 * 날짜 범위 (체크인 ~ 체크아웃)
 */
data class DateRange(
    val checkIn: LocalDate,
    val checkOut: LocalDate
) {
    init {
        require(checkOut.isAfter(checkIn)) {
            "체크아웃은 체크인 이후여야 함"
        }
    }

    val nights: Int get() = (checkOut.toEpochDay() - checkIn.toEpochDay()).toInt()
}

/**
 * 인원 수
 */
data class GuestCount(
    val adults: Int,
    val children: Int = 0
) {
    init {
        require(adults >= 1) { "성인 최소 1명 필요" }
        require(children >= 0) { "어린이 수는 0 이상" }
    }

    val total: Int get() = adults + children
}

/**
 * 취소 사유
 */
data class CancellationReason(
    val cancelledBy: CancelledBy,
    val reason: String
)

enum class CancelledBy {
    GUEST,         // 고객이 취소
    ACCOMMODATION  // 숙소가 취소
}
