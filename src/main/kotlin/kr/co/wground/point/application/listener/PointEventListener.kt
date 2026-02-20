package kr.co.wground.point.application.listener

import kr.co.wground.common.event.AttendanceCheckedEvent
import kr.co.wground.common.event.CommentCreatedEvent
import kr.co.wground.common.event.CommentReactionCreatedEvent
import kr.co.wground.common.event.PostCreatedEvent
import kr.co.wground.common.event.PostReactionCreatedEvent
import kr.co.wground.common.event.StudyApproveEvent
import kr.co.wground.point.application.command.usecase.CreateWalletUseCase
import kr.co.wground.point.application.command.usecase.EarnPointUseCase
import kr.co.wground.user.application.operations.event.DecideUserStatusEvent
import kr.co.wground.user.domain.constant.UserSignupStatus
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class PointEventListener(
    private val earnPointUseCase: EarnPointUseCase,
    private val createWalletUseCase: CreateWalletUseCase,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleUserApproved(event: DecideUserStatusEvent) {
        if (!UserSignupStatus.isAccepted(event.decision)) return

        earnSafely("지갑 생성 (userId: ${event.userId})") {
            createWalletUseCase.createWallets(event.userId)
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handlePostCreated(event: PostCreatedEvent) {
        earnSafely("게시글 작성 (userId: ${event.writerId})") {
            earnPointUseCase.forWritePost(event.writerId, event.postId)
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleCommentCreated(event: CommentCreatedEvent) {
        if(event.commentWriterId == event.postWriterId) return

        earnSafely("댓글 작성 (userId: ${event.commentWriterId})") {
            earnPointUseCase.forWriteComment(event.commentWriterId, event.commentId)
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handlePostReaction(event: PostReactionCreatedEvent) {
        if (event.postWriterId == event.reactorId) return

        earnSafely("게시글 좋아요 받음 (작성자: ${event.postWriterId})") {
            earnPointUseCase.forReceivePostLike(event.postWriterId, event.reactorId)
        }
        earnSafely("게시글 좋아요 누름 (리액터: ${event.reactorId})") {
            earnPointUseCase.forGivePostLike(event.reactorId, event.postId)
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleCommentReaction(event: CommentReactionCreatedEvent) {
        if (event.commentWriterId == event.reactorId) return

        earnSafely("댓글 좋아요 받음 (작성자: ${event.commentWriterId})") {
            earnPointUseCase.forReceiveCommentLike(event.commentWriterId, event.reactorId)
        }
        earnSafely("댓글 좋아요 누름 (리액터: ${event.reactorId})") {
            earnPointUseCase.forGiveCommentLike(event.reactorId, event.commentId)
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleStudyApproved(event: StudyApproveEvent) {
        val memberIds = event.memberIds
            .filter { it != event.leaderId }

        earnSafely("스터디 결재완료 지급 (leaderId: ${event.leaderId})") {
            earnPointUseCase.forStudyCreate(event.leaderId, event.studyId)
        }
        earnSafely("스터디 결재완료 지급 (members: ${memberIds})") {
            if (memberIds.isEmpty()) return@earnSafely
            earnPointUseCase.forStudyJoin(memberIds, event.studyId)
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleAttendanceChecked(event: AttendanceCheckedEvent) {
        earnSafely("출석 포인트 (userId: ${event.userId})") {
            earnPointUseCase.forAttendance(event.userId, event.attendanceDate)
        }
        if (event.streakCount >= 2) {
            earnSafely("연속 출석 보너스 (userId: ${event.userId}, streak: ${event.streakCount})") {
                earnPointUseCase.forAttendanceStreak(event.userId, event.attendanceDate)
            }
        }
    }

    private fun earnSafely(reason: String, action: () -> Unit) {
        try {
            action()
        } catch (e: Exception) {
            log.error("[Point] {} 포인트 적립 실패: {}", reason, e.message)
        }
    }
}