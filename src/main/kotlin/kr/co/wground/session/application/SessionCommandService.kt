package kr.co.wground.session.application

import kr.co.wground.global.common.UserId
import kr.co.wground.session.application.dto.DeviceContext
import kr.co.wground.session.domain.AuthSession
import java.time.LocalDateTime

interface SessionCommandService {
    fun upsertSession(userId: UserId, deviceContext: DeviceContext, expiresAt: LocalDateTime): AuthSession
    fun revokeSession(userId: UserId, sessionId: String)
    fun revokeOtherSessions(userId: UserId, currentSessionId: String)
    fun revokeAllSessions(userId: UserId)
}
