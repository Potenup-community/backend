package kr.co.wground.point.application.listener

import kr.co.wground.common.event.CommentCreatedEvent
import kr.co.wground.common.event.CommentReactionCreatedEvent
import kr.co.wground.common.event.PostCreatedEvent
import kr.co.wground.common.event.PostReactionCreatedEvent
import kr.co.wground.common.event.StudyApproveEvent
import kr.co.wground.point.application.usecase.EarnPointUseCase
import kr.co.wground.study.infra.StudyRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class PointEventListener(
    private val earnPointUseCase: EarnPointUseCase,
    private val studyRepository: StudyRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

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
        earnSafely("댓글 작성 (userId: ${event.commentWriterId})") {
            earnPointUseCase.forWriteComment(event.commentWriterId, event.commentId)
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handlePostReaction(event: PostReactionCreatedEvent) {
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
        val study = studyRepository.findByIdOrNull(event.studyId) ?: run {
            log.warn("[Point] 스터디를 찾을 수 없습니다. (studyId: ${event.studyId})")
            return
        }

        val userIds = study.recruitments
            .map { it.userId }
            .filter { it != study.leaderId }

        earnSafely("스터디 결재완료 지급 (leaderId: ${study.leaderId})") {
            earnPointUseCase.forStudyCreate(study.leaderId, event.studyId)
        }
        earnSafely("스터디 결재완료 지급 (members: ${userIds})") {
            if (userIds.isEmpty()) return@earnSafely
            earnPointUseCase.forStudyJoin(userIds, event.studyId)
        }
    }

    private fun earnSafely(reason: String, action: () -> Unit) {
        try {
            action()
        } catch (e: Exception) {
            log.warn("[Point] {} 포인트 적립 실패: {}", reason, e.message)
        }
    }
}