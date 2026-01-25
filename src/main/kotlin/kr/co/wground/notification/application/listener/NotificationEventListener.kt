package kr.co.wground.notification.application.listener

import kr.co.wground.common.event.AnnouncementCreatedEvent
import kr.co.wground.common.event.CommentCreatedEvent
import kr.co.wground.common.event.CommentReactionCreatedEvent
import kr.co.wground.common.event.MentionCreatedEvent
import kr.co.wground.common.event.PostReactionCreatedEvent
import kr.co.wground.notification.application.command.NotificationCommandService
import kr.co.wground.notification.domain.enums.NotificationType
import kr.co.wground.notification.domain.enums.ReferenceType
import kr.co.wground.notification.domain.vo.NotificationContent
import kr.co.wground.notification.domain.vo.NotificationReference
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class NotificationEventListener(
    private val notificationCommandService: NotificationCommandService,
) {

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleCommentCreated(event: CommentCreatedEvent) {
        if (event.postWriterId == event.commentWriterId) return

        if (event.parentCommentId != null && event.parentCommentWriterId != null) {
            if (event.parentCommentWriterId != event.commentWriterId) {
                notificationCommandService.create(
                    recipientId = event.parentCommentWriterId,
                    actorId = event.commentWriterId,
                    type = NotificationType.COMMENT_REPLY,
                    content = NotificationContent(
                        title = "새 답글",
                        content = "회원님의 댓글에 답글이 달렸습니다.",
                    ),
                    reference = NotificationReference(
                        referenceType = ReferenceType.POST,
                        referenceId = event.postId,
                    ),
                )
            }
            return
        }

        notificationCommandService.create(
            recipientId = event.postWriterId,
            actorId = event.commentWriterId,
            type = NotificationType.POST_COMMENT,
            content = NotificationContent(
                title = "새 댓글",
                content = "회원님의 게시글에 댓글이 달렸습니다.",
            ),
            reference = NotificationReference(
                referenceType = ReferenceType.POST,
                referenceId = event.postId,
            ),
        )
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handlePostReactionCreated(event: PostReactionCreatedEvent) {
        if (event.postWriterId == event.reactorId) return

        notificationCommandService.create(
            recipientId = event.postWriterId,
            actorId = event.reactorId,
            type = NotificationType.POST_REACTION,
            content = NotificationContent(
                title = "게시글 좋아요",
                content = "회원님의 게시글에 좋아요가 눌렸습니다.",
            ),
            reference = NotificationReference(
                referenceType = ReferenceType.POST,
                referenceId = event.postId,
            ),
        )
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleCommentReactionCreated(event: CommentReactionCreatedEvent) {
        if (event.commentWriterId == event.reactorId) return

        notificationCommandService.create(
            recipientId = event.commentWriterId,
            actorId = event.reactorId,
            type = NotificationType.COMMENT_REACTION,
            content = NotificationContent(
                title = "댓글 좋아요",
                content = "회원님의 댓글에 좋아요가 눌렸습니다.",
            ),
            reference = NotificationReference(
                referenceType = ReferenceType.COMMENT,
                referenceId = event.commentId,
            ),
        )
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleMentionCreated(event: MentionCreatedEvent) {
        event.mentionedUserIds
            .filter { it != event.mentionerId }
            .forEach { mentionedUserId ->
                notificationCommandService.create(
                    recipientId = mentionedUserId,
                    actorId = event.mentionerId,
                    type = NotificationType.COMMENT_MENTION,
                    content = NotificationContent(
                        title = "멘션",
                        content = "회원님이 멘션되었습니다.",
                    ),
                    reference = NotificationReference(
                        referenceType = ReferenceType.COMMENT,
                        referenceId = event.commentId,
                    ),
                )
            }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleAnnouncementCreated(event: AnnouncementCreatedEvent) {
        // TODO: 전체 활성 사용자에게 알림 발송
    }
}
