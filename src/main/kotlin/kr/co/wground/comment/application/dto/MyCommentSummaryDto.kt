package kr.co.wground.comment.application.dto

import java.time.LocalDateTime
import kr.co.wground.comment.domain.Comment
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.PostId
import kr.co.wground.post.domain.Post
import kr.co.wground.reaction.application.dto.CommentReactionStats

private const val DELETED_COMMENT_TAG = "[삭제된 댓글]"

data class MyCommentSummaryDto(
    val commentId: CommentId,
    val postId: PostId,
    val content: String,
    val createdAt: LocalDateTime,
    val viewCount: Int,
    val likeCount: Int,
    val isDeleted: Boolean,
) {
    companion object {
        fun from(
            comment: Comment,
            post: Post?,
            reactionStats: CommentReactionStats?,
        ) = MyCommentSummaryDto(
            commentId = comment.id,
            postId = comment.postId,
            content = if (comment.isDeleted) DELETED_COMMENT_TAG else comment.content,
            createdAt = comment.createdAt,
            viewCount = post?.postStatus?.viewCount ?: 0,
            likeCount = reactionStats?.totalCount ?: 0,
            isDeleted = comment.isDeleted,
        )
    }
}
