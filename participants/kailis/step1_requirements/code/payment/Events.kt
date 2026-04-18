package payment

import booking.BookingId
import java.time.LocalDateTime

/**
 * 결제 도메인 이벤트
 */
sealed interface PaymentEvent {
    val paymentId: PaymentId
    val bookingId: BookingId
    val occurredAt: LocalDateTime
}

/** 결제 완료 */
data class PaymentCompleted(
    override val paymentId: PaymentId,
    override val bookingId: BookingId,
    val amount: Money,
    override val occurredAt: LocalDateTime
) : PaymentEvent

/** 환불 처리됨 */
data class PaymentRefunded(
    override val paymentId: PaymentId,
    override val bookingId: BookingId,
    val refundedAmount: Money,
    override val occurredAt: LocalDateTime
) : PaymentEvent
