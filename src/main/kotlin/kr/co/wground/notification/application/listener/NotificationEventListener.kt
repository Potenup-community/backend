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
import kr.co.wground.notification.application.command.BroadcastNotificationCommandService
import kr.co.wground.notification.application.command.NotificationCommandService
import kr.co.wground.notification.application.port.NotificationMessage
import kr.co.wground.notification.application.port.NotificationMessageType
import kr.co.wground.notification.application.port.NotificationSender
import kr.co.wground.notification.domain.enums.BroadcastTargetType
import kr.co.wground.notification.domain.enums.NotificationType
import kr.co.wground.notification.domain.enums.ReferenceType
import kr.co.wground.notification.domain.vo.NotificationReference
import kr.co.wground.notification.exception.NotificationErrorCode
import kr.co.wground.study.domain.constant.RecruitStatus
import kr.co.wground.track.infra.TrackRepository
import kr.co.wground.user.infra.UserRepository
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
    private val broadcastNotificationCommandService: BroadcastNotificationCommandService,
    private val notificationSender: NotificationSender,
    private val trackRepository: TrackRepository,
    private val userRepository: UserRepository,
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
                title = "스터디 지원",
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
                title = "스터디 알림",
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
                    title = "스터디 모집글 삭제",
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
        val placeholders = mapOf(
            "trackName" to track.trackName,
            "months" to "${event.months.month}차",
        )

        // 인앱 알림 (트랙별 브로드캐스트)
        broadcastNotificationCommandService.create(
            type = NotificationType.STUDY_RECRUIT_START,
            title = "스터디 모집 시작",
            targetType = BroadcastTargetType.TRACK,
            targetId = event.trackId,
            placeholders = placeholders,
        )

        // 슬랙 발송
        notificationSender.send(
            NotificationMessage(
                type = NotificationMessageType.STUDY_RECRUIT_START_REMINDER,
                link = studyLink,
                metadata = placeholders,
            )
        )
    }

    @Async(NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleStudyRecruitEnded(event: StudyRecruitEndedEvent) {
        val track = trackRepository.findByIdOrNull(event.trackId) ?: return
        val studyLink = "$frontendUrl/study"
        val placeholders = mapOf(
            "trackName" to track.trackName,
            "months" to "${event.months.month}차",
        )

        // 인앱 알림 (트랙별 브로드캐스트)
        broadcastNotificationCommandService.create(
            type = NotificationType.STUDY_RECRUIT_END,
            title = "스터디 모집 마감",
            targetType = BroadcastTargetType.TRACK,
            targetId = event.trackId,
            placeholders = placeholders,
        )

        // 슬랙 발송
        notificationSender.send(
            NotificationMessage(
                type = NotificationMessageType.STUDY_RECRUIT_END_REMINDER,
                link = studyLink,
                metadata = placeholders,
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
