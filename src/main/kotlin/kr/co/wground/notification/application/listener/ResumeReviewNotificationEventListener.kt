package kr.co.wground.notification.application.listener

import kr.co.wground.common.event.ResumeReviewCompletedEvent
import kr.co.wground.notification.application.command.NotificationCommandService
import kr.co.wground.notification.application.port.NotificationMessage
import kr.co.wground.notification.application.port.NotificationMessageType
import kr.co.wground.notification.application.port.NotificationSender
import kr.co.wground.notification.domain.enums.NotificationType
import kr.co.wground.notification.domain.enums.ReferenceType
import kr.co.wground.notification.domain.vo.NotificationReference
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ResumeReviewNotificationEventListener(
    private val notificationCommandService: NotificationCommandService,
    private val notificationSender: NotificationSender,
    @Value("\${app.frontend-url}") private val frontendUrl: String,
) {

    @Async(NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleResumeReviewCompleted(event: ResumeReviewCompletedEvent) {
        createNotificationSafely {
            notificationCommandService.create(
                recipientId = event.userId,
                actorId = null,
                type = NotificationType.RESUME_REVIEW_COMPLETED,
                title = "이력서 첨삭 완료",
                reference = NotificationReference(
                    referenceId = event.resumeReviewId,
                    referenceType = ReferenceType.RESUME_REVIEW,
                )
            )
        }

        notificationSender.send(
            NotificationMessage(
                type = NotificationMessageType.STUDY_RECRUIT_START_REMINDER,
                link = "$frontendUrl/resume-reviews",
            )
        )
    }
}
