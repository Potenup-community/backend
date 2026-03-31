package kr.co.wground.session.infra

import kr.co.wground.global.common.UserId
import kr.co.wground.session.domain.AuthSession
import kr.co.wground.session.domain.SessionStatus
import org.springframework.data.jpa.repository.JpaRepository

interface AuthSessionJpaRepository : JpaRepository<AuthSession, String> {
    fun findByUserIdAndDeviceId(userId: UserId, deviceId: String): AuthSession?
    fun findAllByUserIdAndStatus(userId: UserId, status: SessionStatus): List<AuthSession>
}
