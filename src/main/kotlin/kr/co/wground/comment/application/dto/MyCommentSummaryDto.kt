package kr.co.wground.comment.application.dto

import java.time.LocalDateTime
import kr.co.wground.comment.domain.Comment
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.application.dto.CommentReactionStats
import kr.co.wground.user.application.operations.constant.NOT_ASSOCIATE
import kr.co.wground.user.application.operations.constant.UNKNOWN_USER_NAME_TAG
import kr.co.wground.user.infra.dto.UserDisplayInfoDto
import kr.co.wground.user.utils.defaultimage.application.constant.AvatarConstants.DEFAULT_AVATAR_PATH

private const val DELETED_COMMENT_TAG = "[삭제된 댓글]"

data class MyCommentSummaryDto(
    val commentId: CommentId,
    val postId: PostId,
    val content: String,
    val authorId: UserId,
    val authorName: String,
    val trackName: String,
    val authorProfileImageUrl: String,
    val createdAt: LocalDateTime,
    val commentReactionStats: CommentReactionStats,
    val isDeleted: Boolean,
) {
    companion object {
        fun from(
            comment: Comment,
            author: UserDisplayInfoDto?,
            reactionStats: CommentReactionStats,
        ) = MyCommentSummaryDto(
            commentId = comment.id,
            postId = comment.postId,
            content = if (comment.isDeleted) DELETED_COMMENT_TAG else comment.content,
            authorId = comment.writerId,
            authorName = author?.name ?: UNKNOWN_USER_NAME_TAG,
            trackName = author?.trackName ?: NOT_ASSOCIATE,
            authorProfileImageUrl = author?.profileImageUrl ?: "",
            createdAt = comment.createdAt,
            commentReactionStats = reactionStats,
            isDeleted = comment.isDeleted,
        )
    }
}
