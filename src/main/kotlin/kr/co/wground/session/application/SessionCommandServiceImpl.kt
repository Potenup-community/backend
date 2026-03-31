package kr.co.wground.session.application

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.session.application.dto.DeviceContext
import kr.co.wground.session.domain.AuthSession
import kr.co.wground.session.domain.event.SessionCreatedEvent
import kr.co.wground.session.domain.event.SessionRevokedEvent
import kr.co.wground.session.domain.repository.AuthSessionRepository
import kr.co.wground.session.exception.SessionErrorCode
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class SessionCommandServiceImpl(
    private val authSessionRepository: AuthSessionRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : SessionCommandService {

    override fun upsertSession(
        userId: UserId,
        deviceContext: DeviceContext,
        expiresAt: LocalDateTime,
    ): AuthSession {
        val existing = authSessionRepository.findByUserIdAndDeviceId(userId, deviceContext.deviceId)

        if (existing != null) {
            existing.renew(expiresAt)
            return existing
        }

        val session = AuthSession(
            sessionId = UUID.randomUUID().toString(),
            userId = userId,
            deviceId = deviceContext.deviceId,
            deviceName = deviceContext.deviceName,
            userAgent = deviceContext.userAgent,
            ipAddress = deviceContext.ipAddress,
            expiresAt = expiresAt,
        )
        authSessionRepository.save(session)

        eventPublisher.publishEvent(
            SessionCreatedEvent(
                sessionId = session.sessionId,
                userId = session.userId,
                deviceId = session.deviceId,
                deviceName = session.deviceName,
            )
        )
        return session
    }

    override fun revokeSession(userId: UserId, sessionId: String) {
        val session = authSessionRepository.findBySessionId(sessionId)
            ?: throw BusinessException(SessionErrorCode.SESSION_NOT_FOUND)

        if (session.userId != userId) {
            throw BusinessException(SessionErrorCode.SESSION_FORBIDDEN)
        }

        session.revoke()

        eventPublisher.publishEvent(SessionRevokedEvent(session.sessionId, session.userId))
    }

    override fun revokeOtherSessions(userId: UserId, currentSessionId: String) {
        val others = authSessionRepository.findAllActiveByUserId(userId)
            .filter { it.sessionId != currentSessionId }

        others.forEach { it.revoke() }
        authSessionRepository.saveAll(others)

        others.forEach { eventPublisher.publishEvent(SessionRevokedEvent(it.sessionId, it.userId)) }
    }

    override fun revokeAllSessions(userId: UserId) {
        val sessions = authSessionRepository.findAllActiveByUserId(userId)

        sessions.forEach { it.revoke() }
        authSessionRepository.saveAll(sessions)

        sessions.forEach { eventPublisher.publishEvent(SessionRevokedEvent(it.sessionId, it.userId)) }
    }
}
