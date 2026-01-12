package kr.co.wground.notification.domain

import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import kr.co.wground.global.common.NotificationId
import kr.co.wground.global.common.RecipientId
import kr.co.wground.notification.domain.enums.NotificationStatus
import kr.co.wground.notification.domain.enums.NotificationType
import kr.co.wground.notification.domain.vo.NotificationContent
import java.time.LocalDateTime
import java.util.UUID

@Entity
class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: NotificationId = 0,
    @Column(unique = true)
    val eventId: UUID,
    val recipient: RecipientId,
    @Embedded
    val content: NotificationContent,
    @Enumerated(EnumType.STRING)
    val type: NotificationType,
    status: NotificationStatus = NotificationStatus.UNREAD,
    @Column(updatable = false, nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val expiresAt: LocalDateTime? //필요 할지 확인 필요
) {
    var status = status
    protected set

    fun markAsRead() {
        if (status.isRead()) return
        status = NotificationStatus.READ
    }

    fun isExpired(now: LocalDateTime): Boolean =
        expiresAt?.isBefore(now) ?: false
}
