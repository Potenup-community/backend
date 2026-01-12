package kr.co.wground.comment.application.dto

import java.time.LocalDateTime
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.application.dto.CommentReactionStats

data class CommentSummaryDto(
    val commentId: CommentId,
    val content: String,
    val authorId: UserId,
    val authorName: String,
    val trackName: String,
    val authorProfileImageUrl: String,
    val createdAt: LocalDateTime,
    val commentReactionStats: CommentReactionStats,
    val isDeleted: Boolean,
    val replies: List<CommentSummaryDto>,
) {
    companion object {
        fun of(
            commentId: CommentId,
            content: String,
            authorId: UserId,
            authorName: String,
            trackName: String,
            authorProfileImageUrl: String,
            createdAt: LocalDateTime,
            commentReactionStats: CommentReactionStats,
            isDeleted: Boolean,
            replies: List<CommentSummaryDto>,
        ): CommentSummaryDto {
            return CommentSummaryDto(
                commentId = commentId,
                content = content,
                authorId = authorId,
                authorName = authorName,
                trackName = trackName,
                authorProfileImageUrl = authorProfileImageUrl,
                createdAt = createdAt,
                commentReactionStats = commentReactionStats,
                isDeleted = isDeleted,
                replies = replies,
            )
        }
    }
}
