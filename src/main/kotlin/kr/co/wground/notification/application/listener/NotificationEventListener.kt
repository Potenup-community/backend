package kr.co.wground.notification.application.listener

import kr.co.wground.common.event.AnnouncementCreatedEvent
import kr.co.wground.common.event.CommentCreatedEvent
import kr.co.wground.common.event.CommentReactionCreatedEvent
import kr.co.wground.common.event.MentionCreatedEvent
import kr.co.wground.common.event.PostReactionCreatedEvent
import kr.co.wground.common.event.StudyDeletedEvent
import kr.co.wground.common.event.StudyDetermineEvent
import kr.co.wground.common.event.StudyRecruitEndedEvent
import kr.co.wground.common.event.StudyRecruitEvent
import kr.co.wground.common.event.StudyRecruitStartedEvent
import kr.co.wground.exception.BusinessException
import kr.co.wground.notification.application.command.NotificationCommandService
import kr.co.wground.notification.application.port.NotificationMessage
import kr.co.wground.notification.application.port.NotificationMessageType
import kr.co.wground.notification.application.port.NotificationSender
import kr.co.wground.notification.domain.enums.NotificationType
import kr.co.wground.notification.domain.enums.ReferenceType
import kr.co.wground.notification.domain.vo.NotificationContent
import kr.co.wground.notification.domain.vo.NotificationReference
import kr.co.wground.notification.exception.NotificationErrorCode
import kr.co.wground.study.domain.constant.RecruitStatus
import kr.co.wground.track.infra.TrackRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

private const val NOTIFICATION_EXECUTOR = "notificationExecutor"

@Component
class NotificationEventListener(
    private val notificationCommandService: NotificationCommandService,
    private val notificationSender: NotificationSender,
    private val trackRepository: TrackRepository,
    @Value("\${app.frontend-url}") private val frontendUrl: String,
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
            }
            return
        }

        createNotificationSafely {
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
                content = NotificationContent(
                    title = "댓글 좋아요",
                    content = "회원님의 댓글에 좋아요가 눌렸습니다.",
                ),
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
        event.mentionUserIds
            .filter { it != event.mentionerId }
            .forEach { mentionedUserId ->
                createNotificationSafely {
                    notificationCommandService.create(
                        recipientId = mentionedUserId,
                        actorId = event.mentionerId,
                        type = NotificationType.COMMENT_MENTION,
                        content = NotificationContent(
                            title = "멘션",
                            content = "회원님이 멘션되었습니다.",
                        ),
                        reference = NotificationReference(
                            referenceType = ReferenceType.POST,
                            referenceId = event.postId,
                            subReferenceId = event.commentId,
                        ),
                    )
                }
            }
    }

    // TODO : 나중에 앱 푸시 알림 추가 예정
    @Async(NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleAnnouncementCreated(event: AnnouncementCreatedEvent) {
        val postLink = "$frontendUrl/post/${event.postId}"
        notificationSender.send(
            NotificationMessage(
                type = NotificationMessageType.ANNOUNCEMENT,
                link = postLink,
                metadata = mapOf(
                    "title" to event.title,
                )
            )
        )
    }

    @Async(NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleStudyRecruit(event: StudyRecruitEvent) {
        createNotificationSafely {
            notificationCommandService.create(
                recipientId = event.leaderId,
                actorId = null,
                type = NotificationType.STUDY_APPLICATION,
                content = NotificationContent(
                    title = "스터디 지원",
                    content = "스터디에 새로운 지원자가 있습니다.",
                ),
                reference = NotificationReference(
                    referenceType = ReferenceType.STUDY,
                    referenceId = event.studyId,
                ),
            )
        }
    }

    @Async(NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleStudyDetermine(event: StudyDetermineEvent) {
        if (event.recruitStatus != RecruitStatus.APPROVED) return

        createNotificationSafely {
            notificationCommandService.create(
                recipientId = event.userId,
                actorId = null,
                type = NotificationType.STUDY_APPROVED,
                content = NotificationContent(
                    title = "스터디 알림",
                    content = "스터디 신청이 승인되었어요! 🎉",
                ),
                reference = NotificationReference(
                    referenceType = ReferenceType.STUDY,
                    referenceId = event.studyId,
                ),
            )
        }
    }

    @Async(NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleStudyDeleted(event: StudyDeletedEvent) {
        event.userIds.forEach { userId ->
            createNotificationSafely {
                notificationCommandService.create(
                    recipientId = userId,
                    actorId = null,
                    type = NotificationType.STUDY_DELETED,
                    content = NotificationContent(
                        title = "스터디 삭제",
                        content = "신청하신 '${event.studyTitle}' 스터디가 삭제되었습니다. 다른 스터디를 찾아주세요 😊",
                    ),
                    reference = NotificationReference(
                        referenceType = ReferenceType.STUDY,
                        referenceId = event.studyId,
                    ),
                )
            }
        }
    }

    @Async(NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleStudyRecruitStarted(event: StudyRecruitStartedEvent) {
        val track = trackRepository.findByIdOrNull(event.trackId) ?: return
        val studyLink = "$frontendUrl/study"

        notificationSender.send(
            NotificationMessage(
                type = NotificationMessageType.STUDY_RECRUIT_START_REMINDER,
                link = studyLink,
                metadata = mapOf(
                    "trackName" to track.trackName,
                    "months" to "${event.months.month}월차",
                )
            )
        )
    }

    @Async(NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleStudyRecruitEnded(event: StudyRecruitEndedEvent) {
        val track = trackRepository.findByIdOrNull(event.trackId) ?: return
        val studyLink = "$frontendUrl/study"

        notificationSender.send(
            NotificationMessage(
                type = NotificationMessageType.STUDY_RECRUIT_END_REMINDER,
                link = studyLink,
                metadata = mapOf(
                    "trackName" to track.trackName,
                    "months" to "${event.months.month}월차",
                )
            )
        )
    }

    private fun createNotificationSafely(action: () -> Unit) {
        try {
            action()
        } catch (e: BusinessException) {
            if (e.code == NotificationErrorCode.DUPLICATE_NOTIFICATION.code) {
                return
            }
            throw e
        }
    }
}
