package kr.co.wground.user.utils.email.event

import java.time.LocalDateTime

data class VerificationEvent(
    val targets: List<VerificationTarget>
) {
    data class VerificationTarget(
        val email: String,
        val username: String,
        val trackName: String,
        val approveAt: LocalDateTime
    )
}
