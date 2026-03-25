package kr.co.wground.session.application.dto

import java.time.LocalDateTime

data class SessionInfo(
    val sessionId: String,
    val deviceName: String?,
    val userAgent: String?,
    val ipAddress: String?,
    val createdAt: LocalDateTime,
    val lastSeenAt: LocalDateTime,
    val isCurrent: Boolean,
)
