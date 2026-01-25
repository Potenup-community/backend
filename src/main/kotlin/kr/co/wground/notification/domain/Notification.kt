package kr.co.wground.notification.domain

import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime
import java.util.UUID
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.NotificationId
import kr.co.wground.global.common.RecipientId
import kr.co.wground.global.common.UserId
import kr.co.wground.notification.domain.enums.NotificationStatus
import kr.co.wground.notification.domain.enums.NotificationType
import kr.co.wground.notification.domain.vo.NotificationContent
import kr.co.wground.notification.domain.vo.NotificationReference
import kr.co.wground.notification.exception.NotificationErrorCode

@Entity
class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: NotificationId = 0,
    @Column(unique = true)
    val eventId: UUID,
    val recipientId: RecipientId,
    val actorId: UserId?,
    @Embedded
    val content: NotificationContent,
    @Embedded
    val reference: NotificationReference?,
    @Enumerated(EnumType.STRING)
    val type: NotificationType,
    status: NotificationStatus = NotificationStatus.UNREAD,
    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val expiresAt: LocalDateTime?,
) {
    init {
        validateRecipientId()
        validateActorId()
        validateExpiresAt()
    }

    var status = status
        protected set

    private fun validateRecipientId() {
        if (recipientId <= 0) {
            throw BusinessException(NotificationErrorCode.INVALID_RECIPIENT_ID)
        }
    }

    private fun validateActorId() {
        actorId?.let {
            if (it <= 0) {
                throw BusinessException(NotificationErrorCode.INVALID_ACTOR_ID)
            }
        }
    }

    private fun validateExpiresAt() {
        expiresAt?.let {
            if (!it.isAfter(createdAt)) {
                throw BusinessException(NotificationErrorCode.INVALID_EXPIRES_AT)
            }
        }
    }

    fun markAsRead() {
        if (status.isRead()) return
        status = NotificationStatus.READ
    }

    fun isExpired(now: LocalDateTime): Boolean =
        expiresAt?.isBefore(now) ?: false
}
