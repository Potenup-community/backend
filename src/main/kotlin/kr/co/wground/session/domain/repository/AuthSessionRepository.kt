package kr.co.wground.session.domain.repository

import kr.co.wground.global.common.UserId
import kr.co.wground.session.domain.AuthSession

interface AuthSessionRepository {
    fun findBySessionId(sessionId: String): AuthSession?
    fun findAllActiveByUserId(userId: UserId): List<AuthSession>
    fun findByUserIdAndDeviceId(userId: UserId, deviceId: String): AuthSession?
    fun save(session: AuthSession)
    fun saveAll(sessions: List<AuthSession>)
}
