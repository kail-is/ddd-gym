package payment

import booking.BookingId
import java.time.LocalDateTime
import java.util.UUID

/**
 * 결제 Aggregate Root
 */
class Payment private constructor(
    val id: PaymentId,
    val bookingId: BookingId,
    val amount: Money,
    private var _status: PaymentStatus,
    private var _refundedAmount: Money = Money.ZERO
) {
    val status: PaymentStatus get() = _status
    val refundedAmount: Money get() = _refundedAmount

    companion object {
        fun create(bookingId: BookingId, amount: Money): Payment {
            return Payment(
                id = PaymentId(UUID.randomUUID()),
                bookingId = bookingId,
                amount = amount,
                _status = PaymentStatus.PENDING
            )
        }
    }

    // 결제 완료
    fun complete(): PaymentCompleted {
        require(_status == PaymentStatus.PENDING) {
            "대기 상태에서만 결제 완료 가능"
        }
        _status = PaymentStatus.COMPLETED
        return PaymentCompleted(id, bookingId, amount, LocalDateTime.now())
    }

    // 전액 환불 (거절 시)
    fun refundFull(): PaymentRefunded {
        require(_status == PaymentStatus.COMPLETED) {
            "결제 완료 상태에서만 환불 가능"
        }
        _status = PaymentStatus.REFUNDED
        _refundedAmount = amount
        return PaymentRefunded(id, bookingId, amount, LocalDateTime.now())
    }

    // 부분 환불 (취소 시 조건부)
    fun refundPartial(refundAmount: Money): PaymentRefunded {
        require(_status == PaymentStatus.COMPLETED) {
            "결제 완료 상태에서만 환불 가능"
        }
        require(refundAmount.value <= amount.value) {
            "환불 금액이 결제 금액을 초과할 수 없음"
        }
        _status = if (refundAmount == amount) {
            PaymentStatus.REFUNDED
        } else {
            PaymentStatus.PARTIALLY_REFUNDED
        }
        _refundedAmount = refundAmount
        return PaymentRefunded(id, bookingId, refundAmount, LocalDateTime.now())
    }
}
