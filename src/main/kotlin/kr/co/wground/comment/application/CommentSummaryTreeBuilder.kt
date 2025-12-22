package kr.co.wground.comment.application

import kr.co.wground.comment.application.dto.CommentSummaryDto
import kr.co.wground.comment.domain.Comment
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.UserId
import kr.co.wground.user.domain.User
import kr.co.wground.user.utils.defaultimage.application.constant.AvatarConstants.DEFAULT_AVATAR_PATH

private const val DELETED_COMMENT_TAG = "[삭제된 댓글]"
private const val UNKNOWN_USER_NAME_TAG = "탈퇴한 사용자"

class CommentSummaryTreeBuilder private constructor(
    private val groupedByParent: Map<CommentId?, List<Comment>>,
    private val usersById: Map<UserId, User>,
    private val reactionCountByCommentId: Map<CommentId, Int>,
) {
    companion object {
        fun from(
            comments: List<Comment>,
            usersById: Map<UserId, User>,
            reactionCountByCommentId: Map<CommentId, Int>,
        ): CommentSummaryTreeBuilder {
            val grouped = comments
                .sortedWith(compareBy<Comment> { it.createdAt }.thenBy { it.id })
                .groupBy { it.parentId }
            return CommentSummaryTreeBuilder(grouped, usersById, reactionCountByCommentId)
        }
    }

    fun build(): List<CommentSummaryDto> {
        return groupedByParent[null]
            .orEmpty()
            .map { buildNode(it) }
    }

    private fun buildNode(comment: Comment): CommentSummaryDto {
        val author = usersById[comment.writerId]
        val content = if (comment.isDeleted) DELETED_COMMENT_TAG else comment.content
        val replies = groupedByParent[comment.id].orEmpty().map { buildNode(it) }

        return CommentSummaryDto.of(
            commentId = comment.id,
            content = content,
            authorId = comment.writerId,
            authorName = author?.name ?: UNKNOWN_USER_NAME_TAG,
            authorProfileImageUrl = author?.accessProfile() ?: DEFAULT_AVATAR_PATH,
            createdAt = comment.createdAt,
            reactionCount = reactionCountByCommentId[comment.id] ?: 0,
            isDeleted = comment.isDeleted,
            replies = replies
        )
    }
}
