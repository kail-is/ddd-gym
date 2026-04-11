package payment

import java.math.BigDecimal
import java.util.UUID

/** 결제 ID */
@JvmInline
value class PaymentId(val value: UUID)

/**
 * 금액
 */
data class Money(val value: BigDecimal) {
    init {
        require(value >= BigDecimal.ZERO) { "금액은 0 이상이어야 함" }
    }

    companion object {
        val ZERO = Money(BigDecimal.ZERO)

        fun of(amount: Long): Money = Money(BigDecimal.valueOf(amount))
    }

    operator fun plus(other: Money): Money = Money(value + other.value)
    operator fun minus(other: Money): Money = Money(value - other.value)
}

/**
 * 결제 상태
 */
enum class PaymentStatus {
    PENDING,            // 대기중
    COMPLETED,          // 완료
    REFUNDED,           // 전액 환불
    PARTIALLY_REFUNDED  // 부분 환불
}
