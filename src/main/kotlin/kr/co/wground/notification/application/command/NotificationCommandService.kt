package kr.co.wground.notification.application.command

import java.time.LocalDateTime
import java.util.UUID
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.NotificationId
import kr.co.wground.global.common.RecipientId
import kr.co.wground.global.common.UserId
import kr.co.wground.notification.domain.Notification
import kr.co.wground.notification.domain.enums.NotificationType
import kr.co.wground.notification.domain.repository.NotificationRepository
import kr.co.wground.notification.domain.vo.NotificationContent
import kr.co.wground.notification.domain.vo.NotificationReference
import kr.co.wground.notification.exception.NotificationErrorCode
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationCommandService(
    private val notificationRepository: NotificationRepository,
) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun create(
        recipientId: RecipientId,
        actorId: UserId?,
        type: NotificationType,
        title: String,
        reference: NotificationReference?,
        placeholders: Map<String, String> = emptyMap(),
        expiresAt: LocalDateTime? = null,
    ) {
        val eventId = UUID.randomUUID()
        val content = NotificationContent.random(type, title, placeholders)

        try {
            notificationRepository.save(
                Notification(
                    eventId = eventId,
                    recipientId = recipientId,
                    actorId = actorId,
                    content = content,
                    reference = reference,
                    type = type,
                    expiresAt = expiresAt,
                )
            )
        } catch (e: DataIntegrityViolationException) {
            throw BusinessException(NotificationErrorCode.DUPLICATE_NOTIFICATION, cause = e)
        }
    }

    @Transactional
    fun markAsRead(notificationId: NotificationId, recipientId: RecipientId) {
        val notification = notificationRepository.findByIdAndRecipientId(notificationId, recipientId)
            ?: throw BusinessException(NotificationErrorCode.NOTIFICATION_NOT_FOUND)
        notification.markAsRead()
    }

    @Transactional
    fun markAllAsRead(recipientId: RecipientId) {
        notificationRepository.markAllAsReadByRecipientId(recipientId)
    }
}

