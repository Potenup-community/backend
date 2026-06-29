package kr.co.wground.notification.application.listener

import kr.co.wground.common.event.CommentCreatedEvent
import kr.co.wground.common.event.CommentReactionCreatedEvent
import kr.co.wground.common.event.MentionCreatedEvent
import kr.co.wground.common.event.PostReactionCreatedEvent
import kr.co.wground.notification.application.command.NotificationCommandService
import kr.co.wground.notification.domain.enums.NotificationType
import kr.co.wground.notification.domain.enums.ReferenceType
import kr.co.wground.notification.domain.vo.NotificationReference
import kr.co.wground.user.infra.UserRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class CommentActivityNotificationEventListener(
    private val notificationCommandService: NotificationCommandService,
    private val userRepository: UserRepository,
) {

    @Async(NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleCommentCreated(event: CommentCreatedEvent) {
        val isSelfComment = event.postWriterId == event.commentWriterId
        if (isSelfComment) return

        val isReplyComment = event.parentCommentId != null && event.parentCommentWriterId != null
        val isSelfReply = event.parentCommentWriterId == event.commentWriterId

        if (isReplyComment) {
            val parentWriterId = event.parentCommentWriterId ?: return
            if (!isSelfReply) {
                createNotificationSafely {
                    notificationCommandService.create(
                        recipientId = parentWriterId,
                        actorId = event.commentWriterId,
                        type = NotificationType.COMMENT_REPLY,
                        title = "새 답글",
                        reference = NotificationReference(
                            referenceType = ReferenceType.POST,
                            referenceId = event.postId,
                        ),
                    )
                }
            }
            return
        }

        createNotificationSafely {
            notificationCommandService.create(
                recipientId = event.postWriterId,
                actorId = event.commentWriterId,
                type = NotificationType.POST_COMMENT,
                title = "새 댓글",
                reference = NotificationReference(
                    referenceType = ReferenceType.POST,
                    referenceId = event.postId,
                ),
            )
        }
    }

    @Async(NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handlePostReactionCreated(event: PostReactionCreatedEvent) {
        if (event.postWriterId == event.reactorId) return

        createNotificationSafely {
            notificationCommandService.create(
                recipientId = event.postWriterId,
                actorId = event.reactorId,
                type = NotificationType.POST_REACTION,
                title = "게시글 좋아요",
                reference = NotificationReference(
                    referenceType = ReferenceType.POST,
                    referenceId = event.postId,
                ),
            )
        }
    }

    @Async(NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleCommentReactionCreated(event: CommentReactionCreatedEvent) {
        if (event.commentWriterId == event.reactorId) return

        createNotificationSafely {
            notificationCommandService.create(
                recipientId = event.commentWriterId,
                actorId = event.reactorId,
                type = NotificationType.COMMENT_REACTION,
                title = "댓글 좋아요",
                reference = NotificationReference(
                    referenceType = ReferenceType.POST,
                    referenceId = event.postId,
                    subReferenceId = event.commentId,
                ),
            )
        }
    }

    @Async(NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleMentionCreated(event: MentionCreatedEvent) {
        val targetUserIds = event.mentionUserIds.filter { it != event.mentionerId }
        if (targetUserIds.isEmpty()) return

        val usersById = userRepository.findAllById(targetUserIds).associateBy { it.userId }

        targetUserIds.forEach { mentionedUserId ->
            createNotificationSafely {
                val mentionedUser = usersById[mentionedUserId]
                val placeholders = if (mentionedUser != null) {
                    mapOf("name" to mentionedUser.name)
                } else {
                    emptyMap()
                }

                notificationCommandService.create(
                    recipientId = mentionedUserId,
                    actorId = event.mentionerId,
                    type = NotificationType.COMMENT_MENTION,
                    title = "멘션",
                    reference = NotificationReference(
                        referenceType = ReferenceType.POST,
                        referenceId = event.postId,
                        subReferenceId = event.commentId,
                    ),
                    placeholders = placeholders,
                )
            }
        }
    }
}
