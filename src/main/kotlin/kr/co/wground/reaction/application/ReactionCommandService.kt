package kr.co.wground.reaction.application

import kr.co.wground.comment.infra.CommentRepository
import kr.co.wground.common.Delta
import kr.co.wground.common.UpdateReactionEvent
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.PostId
import kr.co.wground.reaction.infra.jpa.PostReactionJpaRepository
import kr.co.wground.post.infra.PostRepository
import kr.co.wground.reaction.application.dto.CommentReactCommand
import kr.co.wground.reaction.application.dto.PostReactCommand
import kr.co.wground.reaction.exception.ReactionErrorCode
import kr.co.wground.reaction.infra.jpa.CommentReactionJpaRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class ReactionCommandService(
    private val postReactionJpaRepository: PostReactionJpaRepository,
    private val commentReactionJpaRepository: CommentReactionJpaRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {

    // reacts --------------------

    fun reactToPost(command: PostReactCommand) {
        validatePostExistence(command.postId)

        postReactionJpaRepository.saveIdempotentlyForMysqlOrH2(
            command.userId,
            command.postId,
            command.reactionType,
            LocalDateTime.now()
        )

        eventPublisher.publishEvent(UpdateReactionEvent(
            UUID.randomUUID(), command.postId, Delta.PLUS
        ))
    }

    fun reactToComment(command: CommentReactCommand) {
        validateCommentExistence(command.commentId)

        commentReactionJpaRepository.saveIdempotentlyForMysqlOrH2(
            command.userId,
            command.commentId,
            command.reactionType,
            LocalDateTime.now()
        )
    }

    // unreacts --------------------

    fun unreactToPost(command: PostReactCommand) {
        postReactionJpaRepository.deleteByUserIdAndPostIdAndReactionType(
            command.userId, command.postId, command.reactionType)

        eventPublisher.publishEvent(UpdateReactionEvent(
            UUID.randomUUID(), command.postId, Delta.MINUS
        ))
    }

    fun unreactToComment(command: CommentReactCommand) {
        commentReactionJpaRepository.deleteByUserIdAndCommentIdAndReactionType(
            command.userId, command.commentId, command.reactionType)
    }

    // validation --------------------

    fun validatePostExistence(postId: PostId) {
        if (!postRepository.existsById(postId)) {
            throw BusinessException(ReactionErrorCode.POST_NOT_FOUND)
        }
    }

    fun validateCommentExistence(commentId: CommentId) {
        if (!commentRepository.existsById(commentId)) {
            throw BusinessException(ReactionErrorCode.COMMENT_NOT_FOUND)
        }
    }
}
