package kr.co.wground.notification.application.listener

import kr.co.wground.common.event.StudyDeletedEvent
import kr.co.wground.common.event.StudyRecruitEndedSoonEvent
import kr.co.wground.common.event.StudyRecruitEvent
import kr.co.wground.common.event.StudyRecruitStartedEvent
import kr.co.wground.common.event.StudyRecruitmentEvent
import kr.co.wground.common.event.StudyReportApprovedEvent
import kr.co.wground.common.event.StudyReportRejectedEvent
import kr.co.wground.common.event.StudyReportResubmittedEvent
import kr.co.wground.common.event.StudyReportSubmittedEvent
import kr.co.wground.notification.application.command.BroadcastNotificationCommandService
import kr.co.wground.notification.application.command.NotificationCommandService
import kr.co.wground.notification.application.port.NotificationMessage
import kr.co.wground.notification.application.port.NotificationMessageType
import kr.co.wground.notification.application.port.NotificationSender
import kr.co.wground.notification.domain.enums.BroadcastTargetType
import kr.co.wground.notification.domain.enums.NotificationAudience
import kr.co.wground.notification.domain.enums.NotificationType
import kr.co.wground.notification.domain.enums.ReferenceType
import kr.co.wground.notification.domain.vo.NotificationReference
import kr.co.wground.track.infra.TrackRepository
import kr.co.wground.user.infra.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class StudyNotificationEventListener(
    private val notificationCommandService: NotificationCommandService,
    private val broadcastNotificationCommandService: BroadcastNotificationCommandService,
    private val notificationSender: NotificationSender,
    private val trackRepository: TrackRepository,
    private val userRepository: UserRepository,
    @Value("\${app.frontend-url}") private val frontendUrl: String,
) {

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
    fun handleStudyReportSubmitted(event: StudyReportSubmittedEvent) {
        val adminIds = userRepository.findAllAdmins()
            .map { it.userId }
            .filter { it != event.leaderId }

        adminIds.forEach { adminId ->
            createNotificationSafely {
                notificationCommandService.create(
                    recipientId = adminId,
                    actorId = event.leaderId,
                    type = NotificationType.STUDY_REPORT_SUBMITTED,
                    title = "스터디 결과 보고 상신",
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
    fun handleStudyReportResubmitted(event: StudyReportResubmittedEvent) {
        val adminIds = userRepository.findAllAdmins()
            .map { it.userId }
            .filter { it != event.leaderId }

        adminIds.forEach { adminId ->
            createNotificationSafely {
                notificationCommandService.create(
                    recipientId = adminId,
                    actorId = event.leaderId,
                    type = NotificationType.STUDY_REPORT_RESUBMITTED,
                    title = "스터디 결과 보고 재상신",
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
    fun handleStudyReportApproved(event: StudyReportApprovedEvent) {
        if (event.leaderId == event.adminId) return

        createNotificationSafely {
            notificationCommandService.create(
                recipientId = event.leaderId,
                actorId = event.adminId,
                type = NotificationType.STUDY_REPORT_APPROVED,
                title = "스터디 결과 보고 승인",
                reference = NotificationReference(
                    referenceType = ReferenceType.STUDY,
                    referenceId = event.studyId,
                ),
            )
        }
    }

    @Async(NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleStudyReportRejected(event: StudyReportRejectedEvent) {
        if (event.leaderId == event.adminId) return

        createNotificationSafely {
            notificationCommandService.create(
                recipientId = event.leaderId,
                actorId = event.adminId,
                type = NotificationType.STUDY_REPORT_REJECTED,
                title = "스터디 결과 보고 반려",
                reference = NotificationReference(
                    referenceType = ReferenceType.STUDY,
                    referenceId = event.studyId,
                ),
            )
        }
    }

    @Async(NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleStudyDetermine(event: StudyRecruitmentEvent) {

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
        val trackDisplayName = track.displayName()
        val studyLink = "$frontendUrl/studies"
        val placeholders = mapOf(
            "trackName" to trackDisplayName,
            "months" to "${event.months.month}차",
        )
        val audience = NotificationAudience.fromTrackName(trackDisplayName) ?: NotificationAudience.ALL

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
                audience = audience,
                link = studyLink,
                metadata = placeholders,
            )
        )
    }

    @Async(NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleStudyRecruitEnded(event: StudyRecruitEndedSoonEvent) {
        val track = trackRepository.findByIdOrNull(event.trackId) ?: return
        val trackDisplayName = track.displayName()
        val studyLink = "$frontendUrl/studies"
        val placeholders = mapOf(
            "trackName" to trackDisplayName,
            "months" to "${event.months.month}차",
        )
        val audience = NotificationAudience.fromTrackName(trackDisplayName) ?: NotificationAudience.ALL

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
                audience = audience,
                link = studyLink,
                metadata = placeholders,
            )
        )
    }
}
