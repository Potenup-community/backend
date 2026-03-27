package kr.co.wground.session.infra

import kr.co.wground.global.common.UserId
import kr.co.wground.session.domain.AuthSession
import kr.co.wground.session.domain.SessionStatus
import kr.co.wground.session.domain.repository.AuthSessionRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class AuthSessionRepositoryImpl(
    private val jpaRepository: AuthSessionJpaRepository,
) : AuthSessionRepository {

    override fun findBySessionId(sessionId: String): AuthSession? =
        jpaRepository.findByIdOrNull(sessionId)

    override fun findAllActiveByUserId(userId: UserId): List<AuthSession> =
        jpaRepository.findAllByUserIdAndStatus(userId, SessionStatus.ACTIVE)

    override fun findByUserIdAndDeviceId(userId: UserId, deviceId: String): AuthSession? =
        jpaRepository.findByUserIdAndDeviceId(userId, deviceId)

    override fun save(session: AuthSession) {
        jpaRepository.save(session)
    }

    override fun saveAll(sessions: List<AuthSession>) {
        jpaRepository.saveAll(sessions)
    }
}
