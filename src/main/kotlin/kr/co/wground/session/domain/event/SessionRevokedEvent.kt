package kr.co.wground.session.domain.event

import kr.co.wground.global.common.UserId
import java.time.LocalDateTime

data class SessionRevokedEvent(
    val sessionId: String,
    val userId: UserId,
    val revokedAt: LocalDateTime = LocalDateTime.now(),
)
