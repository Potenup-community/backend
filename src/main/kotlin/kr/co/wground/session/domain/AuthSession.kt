package kr.co.wground.session.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.session.exception.SessionErrorCode
import java.time.LocalDateTime

@Entity
@Table(
    name = "auth_sessions",
    indexes = [
        Index(name = "idx_auth_sessions_user_status", columnList = "user_id, status"),
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_auth_sessions_user_device", columnNames = ["user_id", "device_id"]),
    ],
)
class AuthSession(
    @Id
    @Column(name = "session_id", length = 36, nullable = false, updatable = false)
    val sessionId: String,

    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: UserId,

    @Column(name = "device_id", length = 128, nullable = false, updatable = false)
    val deviceId: String,

    @Column(name = "device_name", length = 128)
    val deviceName: String?,

    @Column(name = "user_agent", length = 512)
    val userAgent: String?,

    @Column(name = "ip_address", length = 64)
    val ipAddress: String?,

    expiresAt: LocalDateTime,
) {
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    var status: SessionStatus = SessionStatus.ACTIVE
        protected set

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "last_seen_at", nullable = false)
    var lastSeenAt: LocalDateTime = LocalDateTime.now()
        protected set

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime = expiresAt
        protected set

    @Column(name = "revoked_at")
    var revokedAt: LocalDateTime? = null
        protected set

    fun isActive(): Boolean = status.isActive() && LocalDateTime.now().isBefore(expiresAt)

    fun revoke() {
        if (status != SessionStatus.ACTIVE) {
            throw BusinessException(SessionErrorCode.SESSION_ALREADY_INACTIVE)
        }
        status = SessionStatus.REVOKED
        revokedAt = LocalDateTime.now()
    }

    fun renew(newExpiresAt: LocalDateTime) {
        status = SessionStatus.ACTIVE
        expiresAt = newExpiresAt
        lastSeenAt = LocalDateTime.now()
        revokedAt = null
    }

    fun touchLastSeen() {
        lastSeenAt = LocalDateTime.now()
    }
}
