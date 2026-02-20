package kr.co.wground.notification.application.listener

import java.time.LocalDateTime
import kr.co.wground.common.event.CommentCreatedEvent
import kr.co.wground.common.event.CommentReactionCreatedEvent
import kr.co.wground.common.event.MentionCreatedEvent
import kr.co.wground.common.event.PostReactionCreatedEvent
import kr.co.wground.common.event.StudyReportApprovedEvent
import kr.co.wground.common.event.StudyReportRejectedEvent
import kr.co.wground.common.event.StudyReportResubmittedEvent
import kr.co.wground.common.event.StudyReportSubmittedEvent
import kr.co.wground.common.event.StudyDeletedEvent
import kr.co.wground.common.event.StudyRecruitEvent
import kr.co.wground.notification.application.command.BroadcastNotificationCommandService
import kr.co.wground.notification.application.command.NotificationCommandService
import kr.co.wground.notification.application.port.NotificationSender
import kr.co.wground.notification.domain.enums.NotificationType
import kr.co.wground.notification.domain.vo.NotificationReference
import kr.co.wground.track.infra.TrackRepository
import kr.co.wground.user.domain.User
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserStatus
import kr.co.wground.user.infra.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class NotificationEventListenerTest {
    @Mock
    private lateinit var notificationCommandService: NotificationCommandService

    @Mock
    private lateinit var broadcastNotificationCommandService: BroadcastNotificationCommandService

    @Mock
    private lateinit var notificationSender: NotificationSender

    @Mock
    private lateinit var trackRepository: TrackRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Captor
    private lateinit var recipientCaptor: ArgumentCaptor<Long>

    @Captor
    private lateinit var actorCaptor: ArgumentCaptor<Long?>

    @Captor
    private lateinit var typeCaptor: ArgumentCaptor<NotificationType>

    @Captor
    private lateinit var titleCaptor: ArgumentCaptor<String>

    @Captor
    private lateinit var referenceCaptor: ArgumentCaptor<NotificationReference?>

    @Captor
    private lateinit var placeholdersCaptor: ArgumentCaptor<Map<String, String>>

    @Captor
    private lateinit var expiresAtCaptor: ArgumentCaptor<LocalDateTime?>

    private lateinit var listener: NotificationEventListener

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        listener = NotificationEventListener(
            notificationCommandService,
            broadcastNotificationCommandService,
            notificationSender,
            trackRepository,
            userRepository,
            "http://frontend-url"
        )
    }

    @Nested
    @DisplayName("댓글 생성 이벤트 처리")
    inner class HandleCommentCreated {

        @DisplayName("게시글 작성자와 댓글 작성자가 다르면 알림이 생성된다")
        @Test
        fun shouldCreateNotification_whenDifferentWriters() {
            // given
            val event = CommentCreatedEvent(
                postId = 1L,
                postWriterId = 100L,
                commentId = 10L,
                commentWriterId = 200L,
                parentCommentId = null,
                parentCommentWriterId = null
            )

            // when
            listener.handleCommentCreated(event)

            // then
            verify(notificationCommandService).create(
                capture(recipientCaptor),
                capture(actorCaptor),
                capture(typeCaptor),
                capture(titleCaptor),
                capture(referenceCaptor),
                capture(placeholdersCaptor),
                capture(expiresAtCaptor)
            )

            assertThat(recipientCaptor.value).isEqualTo(100L)
            assertThat(actorCaptor.value).isEqualTo(200L)
            assertThat(typeCaptor.value).isEqualTo(NotificationType.POST_COMMENT)
        }

        @DisplayName("게시글 작성자와 댓글 작성자가 같으면 알림이 생성되지 않는다")
        @Test
        fun shouldNotCreateNotification_whenSameWriter() {
            // given
            val event = CommentCreatedEvent(
                postId = 1L,
                postWriterId = 100L,
                commentId = 10L,
                commentWriterId = 100L,
                parentCommentId = null,
                parentCommentWriterId = null
            )

            // when
            listener.handleCommentCreated(event)

            // then
            verifyNoInteractions(notificationCommandService)
        }

        @DisplayName("대댓글인 경우 부모 댓글 작성자에게 알림이 생성된다")
        @Test
        fun shouldNotifyParentCommentWriter_whenReply() {
            // given
            val event = CommentCreatedEvent(
                postId = 1L,
                postWriterId = 100L,
                commentId = 10L,
                commentWriterId = 200L,
                parentCommentId = 5L,
                parentCommentWriterId = 300L
            )

            // when
            listener.handleCommentCreated(event)

            // then
            verify(notificationCommandService).create(
                capture(recipientCaptor),
                capture(actorCaptor),
                capture(typeCaptor),
                capture(titleCaptor),
                capture(referenceCaptor),
                capture(placeholdersCaptor),
                capture(expiresAtCaptor)
            )

            assertThat(recipientCaptor.value).isEqualTo(300L)
            assertThat(typeCaptor.value).isEqualTo(NotificationType.COMMENT_REPLY)
        }
    }

    @Nested
    @DisplayName("게시글 좋아요 이벤트 처리")
    inner class HandlePostReactionCreated {

        @DisplayName("게시글 작성자와 좋아요 누른 사람이 다르면 알림이 생성된다")
        @Test
        fun shouldCreateNotification_whenDifferentUsers() {
            // given
            val event = PostReactionCreatedEvent(
                postId = 1L,
                postWriterId = 100L,
                reactorId = 200L
            )

            // when
            listener.handlePostReactionCreated(event)

            // then
            verify(notificationCommandService).create(
                capture(recipientCaptor),
                capture(actorCaptor),
                capture(typeCaptor),
                capture(titleCaptor),
                capture(referenceCaptor),
                capture(placeholdersCaptor),
                capture(expiresAtCaptor)
            )

            assertThat(typeCaptor.value).isEqualTo(NotificationType.POST_REACTION)
        }

        @DisplayName("자신의 게시글에 좋아요하면 알림이 생성되지 않는다")
        @Test
        fun shouldNotCreateNotification_whenSameUser() {
            // given
            val event = PostReactionCreatedEvent(
                postId = 1L,
                postWriterId = 100L,
                reactorId = 100L
            )

            // when
            listener.handlePostReactionCreated(event)

            // then
            verifyNoInteractions(notificationCommandService)
        }
    }

    @Nested
    @DisplayName("댓글 좋아요 이벤트 처리")
    inner class HandleCommentReactionCreated {

        @DisplayName("댓글 작성자와 좋아요 누른 사람이 다르면 알림이 생성된다")
        @Test
        fun shouldCreateNotification_whenDifferentUsers() {
            // given
            val event = CommentReactionCreatedEvent(
                postId = 1L,
                commentId = 10L,
                commentWriterId = 100L,
                reactorId = 200L
            )

            // when
            listener.handleCommentReactionCreated(event)

            // then
            verify(notificationCommandService).create(
                capture(recipientCaptor),
                capture(actorCaptor),
                capture(typeCaptor),
                capture(titleCaptor),
                capture(referenceCaptor),
                capture(placeholdersCaptor),
                capture(expiresAtCaptor)
            )

            assertThat(typeCaptor.value).isEqualTo(NotificationType.COMMENT_REACTION)
        }
    }

    @Nested
    @DisplayName("멘션 이벤트 처리")
    inner class HandleMentionCreated {

        @DisplayName("멘션된 사용자들에게 알림이 생성된다")
        @Test
        fun shouldCreateNotificationForEachMentionedUser() {
            // given
            val event = MentionCreatedEvent(
                postId = 1L,
                commentId = 10L,
                mentionerId = 100L,
                mentionUserIds = listOf(200L, 300L)
            )
            `when`(userRepository.findAllById(listOf(200L, 300L))).thenReturn(emptyList())

            // when
            listener.handleMentionCreated(event)

            // then
            verify(notificationCommandService, Mockito.times(2)).create(
                capture(recipientCaptor),
                capture(actorCaptor),
                capture(typeCaptor),
                capture(titleCaptor),
                capture(referenceCaptor),
                capture(placeholdersCaptor),
                capture(expiresAtCaptor)
            )
        }

        @DisplayName("자기 자신을 멘션하면 알림이 생성되지 않는다")
        @Test
        fun shouldNotCreateNotification_whenMentionSelf() {
            // given
            val event = MentionCreatedEvent(
                postId = 1L,
                commentId = 10L,
                mentionerId = 100L,
                mentionUserIds = listOf(100L)
            )

            // when
            listener.handleMentionCreated(event)

            // then
            verifyNoInteractions(notificationCommandService)
        }
    }

    @Nested
    @DisplayName("스터디 이벤트 처리")
    inner class HandleStudyEvents {

        @DisplayName("스터디 지원 시 리더에게 알림이 생성된다")
        @Test
        fun shouldCreateNotification_whenStudyRecruit() {
            // given
            val event = StudyRecruitEvent(
                studyId = 1L,
                leaderId = 100L
            )

            // when
            listener.handleStudyRecruit(event)

            // then
            verify(notificationCommandService).create(
                capture(recipientCaptor),
                capture(actorCaptor),
                capture(typeCaptor),
                capture(titleCaptor),
                capture(referenceCaptor),
                capture(placeholdersCaptor),
                capture(expiresAtCaptor)
            )

            assertThat(recipientCaptor.value).isEqualTo(100L)
            assertThat(typeCaptor.value).isEqualTo(NotificationType.STUDY_APPLICATION)
        }

        @DisplayName("스터디 삭제(취소) 시 지원자들에게 알림이 생성된다")
        @Test
        fun shouldCreateNotificationForEachRecruit_whenStudyDeleted() {
            // given
            val event = StudyDeletedEvent(
                studyId = 1L,
                studyTitle = "테스트 스터디",
                userIds = listOf(200L, 300L)
            )

            // when
            listener.handleStudyDeleted(event)

            // then
            verify(notificationCommandService, times(2)).create(
                capture(recipientCaptor),
                capture(actorCaptor),
                capture(typeCaptor),
                capture(titleCaptor),
                capture(referenceCaptor),
                capture(placeholdersCaptor),
                capture(expiresAtCaptor)
            )

            assertThat(typeCaptor.value).isEqualTo(NotificationType.STUDY_DELETED)
        }

        @DisplayName("스터디 결과 보고 상신 시 관리자들에게 알림이 생성된다")
        @Test
        fun shouldCreateNotificationForAdmins_whenStudyReportSubmitted() {
            // given
            val leaderId = 100L
            val event = StudyReportSubmittedEvent(
                studyId = 1L,
                leaderId = leaderId,
            )
            `when`(userRepository.findAllAdmins()).thenReturn(
                listOf(
                    createUser(userId = 1L, role = UserRole.ADMIN),
                    createUser(userId = 2L, role = UserRole.ADMIN),
                    createUser(userId = leaderId, role = UserRole.ADMIN),
                )
            )

            // when
            listener.handleStudyReportSubmitted(event)

            // then
            verify(notificationCommandService, times(2)).create(
                capture(recipientCaptor),
                capture(actorCaptor),
                capture(typeCaptor),
                capture(titleCaptor),
                capture(referenceCaptor),
                capture(placeholdersCaptor),
                capture(expiresAtCaptor)
            )

            assertThat(recipientCaptor.allValues).containsExactlyInAnyOrder(1L, 2L)
            assertThat(actorCaptor.allValues).containsOnly(leaderId)
            assertThat(typeCaptor.allValues).containsOnly(NotificationType.STUDY_REPORT_SUBMITTED)
        }

        @DisplayName("스터디 결과 보고 재상신 시 관리자들에게 알림이 생성된다")
        @Test
        fun shouldCreateNotificationForAdmins_whenStudyReportResubmitted() {
            // given
            val leaderId = 100L
            val event = StudyReportResubmittedEvent(
                studyId = 1L,
                leaderId = leaderId,
            )
            `when`(userRepository.findAllAdmins()).thenReturn(
                listOf(
                    createUser(userId = 9L, role = UserRole.ADMIN),
                    createUser(userId = leaderId, role = UserRole.ADMIN),
                )
            )

            // when
            listener.handleStudyReportResubmitted(event)

            // then
            verify(notificationCommandService).create(
                capture(recipientCaptor),
                capture(actorCaptor),
                capture(typeCaptor),
                capture(titleCaptor),
                capture(referenceCaptor),
                capture(placeholdersCaptor),
                capture(expiresAtCaptor)
            )

            assertThat(recipientCaptor.value).isEqualTo(9L)
            assertThat(actorCaptor.value).isEqualTo(leaderId)
            assertThat(typeCaptor.value).isEqualTo(NotificationType.STUDY_REPORT_RESUBMITTED)
        }

        @DisplayName("스터디 결과 보고 승인 시 스터디장에게 알림이 생성된다")
        @Test
        fun shouldCreateNotificationForLeader_whenStudyReportApproved() {
            // given
            val event = StudyReportApprovedEvent(
                studyId = 1L,
                leaderId = 100L,
                adminId = 1L,
            )

            // when
            listener.handleStudyReportApproved(event)

            // then
            verify(notificationCommandService).create(
                capture(recipientCaptor),
                capture(actorCaptor),
                capture(typeCaptor),
                capture(titleCaptor),
                capture(referenceCaptor),
                capture(placeholdersCaptor),
                capture(expiresAtCaptor)
            )

            assertThat(recipientCaptor.value).isEqualTo(100L)
            assertThat(actorCaptor.value).isEqualTo(1L)
            assertThat(typeCaptor.value).isEqualTo(NotificationType.STUDY_REPORT_APPROVED)
        }

        @DisplayName("승인자와 스터디장이 같은 경우 승인 알림이 생성되지 않는다")
        @Test
        fun shouldNotCreateNotification_whenLeaderAndAdminAreSameOnApprove() {
            // given
            val event = StudyReportApprovedEvent(
                studyId = 1L,
                leaderId = 1L,
                adminId = 1L,
            )

            // when
            listener.handleStudyReportApproved(event)

            // then
            verifyNoInteractions(notificationCommandService)
        }

        @DisplayName("스터디 결과 보고 반려 시 스터디장에게 알림이 생성된다")
        @Test
        fun shouldCreateNotificationForLeader_whenStudyReportRejected() {
            // given
            val event = StudyReportRejectedEvent(
                studyId = 1L,
                leaderId = 100L,
                adminId = 1L,
            )

            // when
            listener.handleStudyReportRejected(event)

            // then
            verify(notificationCommandService).create(
                capture(recipientCaptor),
                capture(actorCaptor),
                capture(typeCaptor),
                capture(titleCaptor),
                capture(referenceCaptor),
                capture(placeholdersCaptor),
                capture(expiresAtCaptor)
            )

            assertThat(recipientCaptor.value).isEqualTo(100L)
            assertThat(actorCaptor.value).isEqualTo(1L)
            assertThat(typeCaptor.value).isEqualTo(NotificationType.STUDY_REPORT_REJECTED)
        }

        @DisplayName("승인자와 스터디장이 같은 경우 반려 알림이 생성되지 않는다")
        @Test
        fun shouldNotCreateNotification_whenLeaderAndAdminAreSameOnReject() {
            // given
            val event = StudyReportRejectedEvent(
                studyId = 1L,
                leaderId = 1L,
                adminId = 1L,
            )

            // when
            listener.handleStudyReportRejected(event)

            // then
            verifyNoInteractions(notificationCommandService)
        }
    }

    private fun createUser(userId: Long, role: UserRole): User {
        return User(
            userId = userId,
            trackId = 1L,
            email = "user-$userId@test.com",
            name = "유저$userId",
            phoneNumber = "010-0000-${userId.toString().padStart(4, '0').takeLast(4)}",
            provider = "GOOGLE",
            role = role,
            status = UserStatus.ACTIVE,
        )
    }

    private fun <T> capture(captor: ArgumentCaptor<T>): T = captor.capture()
}
