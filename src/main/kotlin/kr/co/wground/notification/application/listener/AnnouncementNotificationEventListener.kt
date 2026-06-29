package kr.co.wground.notification.application.listener

import kr.co.wground.common.event.AnnouncementCreatedEvent
import kr.co.wground.notification.application.command.BroadcastNotificationCommandService
import kr.co.wground.notification.application.port.NotificationMessage
import kr.co.wground.notification.application.port.NotificationMessageType
import kr.co.wground.notification.application.port.NotificationSender
import kr.co.wground.notification.domain.enums.BroadcastTargetType
import kr.co.wground.notification.domain.enums.NotificationAudience
import kr.co.wground.notification.domain.enums.NotificationType
import kr.co.wground.notification.domain.enums.ReferenceType
import kr.co.wground.notification.domain.vo.NotificationReference
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class AnnouncementNotificationEventListener(
    private val broadcastNotificationCommandService: BroadcastNotificationCommandService,
    private val notificationSender: NotificationSender,
    @Value("\${app.frontend-url}") private val frontendUrl: String,
) {

    @Async(NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleAnnouncementCreated(event: AnnouncementCreatedEvent) {
        // 인앱 알림 (전체 브로드캐스트)
        broadcastNotificationCommandService.create(
            type = NotificationType.ANNOUNCEMENT,
            title = "공지사항",
            targetType = BroadcastTargetType.ALL,
            reference = NotificationReference(
                referenceType = ReferenceType.POST,
                referenceId = event.postId,
            ),
        )

        // 슬랙 발송
        val postLink = "$frontendUrl/post/${event.postId}"
        notificationSender.send(
            NotificationMessage(
                type = NotificationMessageType.ANNOUNCEMENT,
                audience = NotificationAudience.ALL,
                link = postLink,
                metadata = mapOf(
                    "title" to event.title,
                )
            )
        )
    }
}
