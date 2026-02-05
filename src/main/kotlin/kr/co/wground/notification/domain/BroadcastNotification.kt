package kr.co.wground.notification.domain

import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import kr.co.wground.exception.BusinessException
import kr.co.wground.notification.domain.enums.BroadcastTargetType
import kr.co.wground.notification.domain.enums.NotificationType
import kr.co.wground.notification.domain.vo.NotificationContent
import kr.co.wground.notification.domain.vo.NotificationReference
import kr.co.wground.notification.exception.NotificationErrorCode
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "broadcast_notification",
    indexes = [
        Index(name = "idx_broadcast_target", columnList = "target_type, target_id"),
        Index(name = "idx_broadcast_created", columnList = "created_at DESC")
    ]
)
class BroadcastNotification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val eventId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: NotificationType,

    @Embedded
    val content: NotificationContent,

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    val targetType: BroadcastTargetType,

    @Column(name = "target_id")
    val targetId: Long? = null,

    @Embedded
    val reference: NotificationReference?,

    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    val expiresAt: LocalDateTime? = null,
) {
    init {
        validateTarget()
    }

    private fun validateTarget() {
        if (targetType == BroadcastTargetType.TRACK && targetId == null) {
            throw BusinessException(NotificationErrorCode.INVALID_BROADCAST_TARGET)
        }
    }

    fun isExpired(now: LocalDateTime = LocalDateTime.now()): Boolean =
        expiresAt?.isBefore(now) ?: false
}
