package kr.co.wground.notification.application.command

import kr.co.wground.common.NotificationCreateEvent
import kr.co.wground.notification.application.CreateNotificationUseCase
import kr.co.wground.notification.domain.Notification
import kr.co.wground.notification.domain.repository.NotificationRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Service
class NotificationCommandService(
    private val notificationRepository: NotificationRepository,
): CreateNotificationUseCase {

    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    override fun createNotification(event: NotificationCreateEvent) {
        if (notificationRepository.findByEventId(event.eventId)) return

        try {
            notificationRepository.save(
                Notification(
                    eventId = event.eventId,
                    recipient = event.recipient,
                    content = event.content,
                    type = event.type,
                    expiresAt = event.expiresAt,
                )
            )
        } catch (e: DataIntegrityViolationException) {
            return
        }
    }
}
