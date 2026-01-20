package kr.co.wground.comment.application.dto

import java.time.LocalDateTime
import kr.co.wground.comment.domain.Comment
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.PostId
import kr.co.wground.reaction.application.dto.CommentReactionStats

private const val DELETED_COMMENT_TAG = "[삭제된 댓글]"

data class LikedCommentSummaryDto(
    val commentId: CommentId,
    val postId: PostId,
    val content: String,
    val createdAt: LocalDateTime,
    val likedAt: LocalDateTime,
    val commentReactionStats: CommentReactionStats,
    val isDeleted: Boolean,
) {
    companion object {
        fun from(
            comment: Comment,
            reactionStats: CommentReactionStats?,
            likedAt: LocalDateTime,
        ) = LikedCommentSummaryDto(
            commentId = comment.id,
            postId = comment.postId,
            content = if (comment.isDeleted) DELETED_COMMENT_TAG else comment.content,
            createdAt = comment.createdAt,
            likedAt = likedAt,
            commentReactionStats = reactionStats ?: CommentReactionStats.emptyOf(comment.id),
            isDeleted = comment.isDeleted,
        )
    }
}

