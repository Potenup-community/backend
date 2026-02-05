package kr.co.wground.notification.application.command

import kr.co.wground.notification.domain.BroadcastNotification
import kr.co.wground.notification.domain.BroadcastNotificationRead
import kr.co.wground.notification.domain.enums.BroadcastTargetType
import kr.co.wground.notification.domain.enums.NotificationMessageVariant
import kr.co.wground.notification.domain.enums.NotificationType
import kr.co.wground.notification.domain.repository.BroadcastNotificationReadRepository
import kr.co.wground.notification.domain.repository.BroadcastNotificationRepository
import kr.co.wground.notification.domain.vo.NotificationContent
import kr.co.wground.notification.domain.vo.NotificationReference
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class BroadcastNotificationCommandService(
    private val broadcastNotificationRepository: BroadcastNotificationRepository,
    private val broadcastNotificationReadRepository: BroadcastNotificationReadRepository,
) {

    fun create(
        type: NotificationType,
        title: String,
        targetType: BroadcastTargetType,
        targetId: Long? = null,
        reference: NotificationReference? = null,
        placeholders: Map<String, String> = emptyMap(),
        expiresAt: LocalDateTime? = null,
    ): BroadcastNotification {
        val eventId = UUID.randomUUID()
        val message = if (placeholders.isEmpty()) {
            NotificationMessageVariant.getRandomMessage(type)
        } else {
            NotificationMessageVariant.getRandomMessage(type, placeholders)
        }
        val content = NotificationContent(title = title, content = message)

        return broadcastNotificationRepository.save(
            BroadcastNotification(
                eventId = eventId,
                type = type,
                content = content,
                targetType = targetType,
                targetId = targetId,
                reference = reference,
                expiresAt = expiresAt,
            )
        )
    }

    fun markAsRead(userId: Long, notificationId: Long) {
        if (broadcastNotificationReadRepository.existsByUserIdAndNotificationId(userId, notificationId)) {
            return
        }

        broadcastNotificationReadRepository.save(
            BroadcastNotificationRead(
                userId = userId,
                notificationId = notificationId,
            )
        )
    }
}
