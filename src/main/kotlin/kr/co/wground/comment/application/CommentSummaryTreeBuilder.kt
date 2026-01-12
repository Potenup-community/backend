package kr.co.wground.comment.application

import kr.co.wground.comment.application.dto.CommentSummaryDto
import kr.co.wground.comment.domain.Comment
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.application.dto.CommentReactionStats
import kr.co.wground.user.application.operations.constant.NOT_ASSOCIATE
import kr.co.wground.user.application.operations.constant.UNKNOWN_USER_NAME_TAG
import kr.co.wground.user.infra.dto.UserDisplayInfoDto
import kr.co.wground.user.utils.defaultimage.application.constant.AvatarConstants.DEFAULT_AVATAR_PATH

private const val DELETED_COMMENT_TAG = "[삭제된 댓글]"

class CommentSummaryTreeBuilder private constructor(
    private val groupedByParent: Map<CommentId?, List<Comment>>,
    private val usersById: Map<UserId, UserDisplayInfoDto>,
    private val reactionStatsByCommentId: Map<CommentId, CommentReactionStats>,
) {
    companion object {
        fun from(
            comments: List<Comment>,
            usersById: Map<UserId, UserDisplayInfoDto>,
            reactionStatsByCommentId: Map<CommentId, CommentReactionStats>,
        ): CommentSummaryTreeBuilder {
            val grouped = comments
                .sortedWith(compareBy<Comment> { it.createdAt }.thenBy { it.id })
                .groupBy { it.parentId }
            return CommentSummaryTreeBuilder(grouped, usersById, reactionStatsByCommentId)
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
            trackName = author?.trackName ?: NOT_ASSOCIATE,
            authorProfileImageUrl = author?.profileImageUrl ?: DEFAULT_AVATAR_PATH,
            createdAt = comment.createdAt,
            commentReactionStats = reactionStatsByCommentId[comment.id] ?: CommentReactionStats.emptyOf(comment.id),
            isDeleted = comment.isDeleted,
            replies = replies
        )
    }
}
