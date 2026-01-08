package kr.co.wground.comment.presentation.response

import java.time.LocalDateTime
import kr.co.wground.comment.application.dto.CommentSummaryDto
import kr.co.wground.global.common.CommentId
import kr.co.wground.reaction.application.dto.CommentReactionStats

data class CommentSummaryResponse(
    val commentId: CommentId,
    val content: String,
    val author: CommentAuthorResponse,
    val createdAt: LocalDateTime,
    val commentReactionStats: CommentReactionStats,
    val isDeleted: Boolean,
    val replies: List<CommentSummaryResponse>,
) {
    companion object {
        fun from(dto: CommentSummaryDto): CommentSummaryResponse {
            return CommentSummaryResponse(
                commentId = dto.commentId,
                content = dto.content,
                author = CommentAuthorResponse(
                    userId = dto.authorId,
                    name = dto.authorName,
                    trackName = dto.trackName,
                    profileImageUrl = dto.authorProfileImageUrl,
                ),
                createdAt = dto.createdAt,
                commentReactionStats = dto.commentReactionStats,
                isDeleted = dto.isDeleted,
                replies = dto.replies.map { from(it) },
            )
        }
    }
}
