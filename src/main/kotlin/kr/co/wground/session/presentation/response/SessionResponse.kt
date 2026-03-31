package kr.co.wground.session.presentation.response

import kr.co.wground.session.application.dto.SessionInfo
import java.time.LocalDateTime

data class SessionResponse(
    val sessionId: String,
    val deviceName: String?,
    val userAgent: String?,
    val ipAddress: String?,
    val createdAt: LocalDateTime,
    val lastSeenAt: LocalDateTime,
    val isCurrent: Boolean,
) {
    companion object {
        fun from(info: SessionInfo) = SessionResponse(
            sessionId = info.sessionId,
            deviceName = info.deviceName,
            userAgent = info.userAgent,
            ipAddress = info.ipAddress,
            createdAt = info.createdAt,
            lastSeenAt = info.lastSeenAt,
            isCurrent = info.isCurrent,
        )
    }
}

data class SessionListResponse(val sessions: List<SessionResponse>)
