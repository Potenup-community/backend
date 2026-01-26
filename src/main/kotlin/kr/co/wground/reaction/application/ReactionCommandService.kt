package kr.co.wground.reaction.application

import kr.co.wground.comment.domain.Comment
import kr.co.wground.comment.infra.CommentRepository
import kr.co.wground.common.Delta
import kr.co.wground.common.event.CommentReactionCreatedEvent
import kr.co.wground.common.event.PostReactionCreatedEvent
import kr.co.wground.common.event.UpdateReactionEvent
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.PostId
import kr.co.wground.post.domain.Post
import kr.co.wground.post.infra.PostRepository
import kr.co.wground.reaction.application.dto.CommentReactCommand
import kr.co.wground.reaction.application.dto.PostReactCommand
import kr.co.wground.reaction.exception.ReactionErrorCode
import kr.co.wground.reaction.infra.jpa.CommentReactionJpaRepository
import kr.co.wground.reaction.infra.jpa.PostReactionJpaRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
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
        val post = findPostOrThrow(command.postId)

        postReactionJpaRepository.saveIdempotentlyForMysqlOrH2(
            command.userId,
            command.postId,
            command.reactionType,
            LocalDateTime.now()
        )

        eventPublisher.publishEvent(
            UpdateReactionEvent(
                UUID.randomUUID(), command.postId, Delta.PLUS
            )
        )

        eventPublisher.publishEvent(
            PostReactionCreatedEvent(
                postId = command.postId,
                postWriterId = post.writerId,
                reactorId = command.userId,
            )
        )
    }

    fun reactToComment(command: CommentReactCommand) {
        val comment = findCommentOrThrow(command.commentId)

        commentReactionJpaRepository.saveIdempotentlyForMysqlOrH2(
            command.userId,
            command.commentId,
            command.reactionType,
            LocalDateTime.now()
        )

        eventPublisher.publishEvent(
            CommentReactionCreatedEvent(
                postId = comment.postId,
                commentId = command.commentId,
                commentWriterId = comment.writerId,
                reactorId = command.userId,
            )
        )
    }

    // unreacts --------------------

    fun unreactToPost(command: PostReactCommand) {
        postReactionJpaRepository.deleteByUserIdAndPostIdAndReactionType(
            command.userId, command.postId, command.reactionType
        )

        eventPublisher.publishEvent(
            UpdateReactionEvent(
                UUID.randomUUID(), command.postId, Delta.MINUS
            )
        )
    }

    fun unreactToComment(command: CommentReactCommand) {
        commentReactionJpaRepository.deleteByUserIdAndCommentIdAndReactionType(
            command.userId, command.commentId, command.reactionType
        )
    }

    private fun findPostOrThrow(postId: PostId): Post {
        return postRepository.findByIdOrNull(postId)
            ?: throw BusinessException(ReactionErrorCode.POST_NOT_FOUND)
    }

    private fun findCommentOrThrow(commentId: CommentId): Comment {
        return commentRepository.findByIdOrNull(commentId)
            ?: throw BusinessException(ReactionErrorCode.COMMENT_NOT_FOUND)
    }
}
