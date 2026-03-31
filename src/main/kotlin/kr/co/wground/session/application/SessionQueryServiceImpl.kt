package kr.co.wground.session.application

import kr.co.wground.global.common.UserId
import kr.co.wground.session.application.dto.SessionInfo
import kr.co.wground.session.domain.repository.AuthSessionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SessionQueryServiceImpl(
    private val authSessionRepository: AuthSessionRepository,
) : SessionQueryService {

    override fun getActiveSessions(userId: UserId, currentSessionId: String): List<SessionInfo> =
        authSessionRepository.findAllActiveByUserId(userId)
            .filter { it.isActive() }
            .map { session ->
                SessionInfo(
                    sessionId = session.sessionId,
                    deviceName = session.deviceName,
                    userAgent = session.userAgent,
                    ipAddress = session.ipAddress,
                    createdAt = session.createdAt,
                    lastSeenAt = session.lastSeenAt,
                    isCurrent = session.sessionId == currentSessionId,
                )
            }
            .sortedByDescending { it.lastSeenAt }
}
