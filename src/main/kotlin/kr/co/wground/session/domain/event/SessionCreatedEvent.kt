package kr.co.wground.session.domain.event

import kr.co.wground.global.common.UserId
import java.time.LocalDateTime

data class SessionCreatedEvent(
    val sessionId: String,
    val userId: UserId,
    val deviceId: String,
    val deviceName: String?,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
