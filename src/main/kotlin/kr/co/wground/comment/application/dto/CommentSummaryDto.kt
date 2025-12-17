package kr.co.wground.comment.application.dto

import java.time.LocalDateTime
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.UserId

data class CommentSummaryDto(
    val commentId: CommentId,
    val content: String,
    val authorId: UserId,
    val authorName: String,
    val authorProfileImageUrl: String?,
    val createdAt: LocalDateTime,
    val likeCount: Int,
    val isDeleted: Boolean,
    val replies: List<CommentSummaryDto>,
) {
    companion object {
        fun of(
            commentId: CommentId,
            content: String,
            authorId: UserId,
            authorName: String,
            authorProfileImageUrl: String?,
            createdAt: LocalDateTime,
            likeCount: Int = 0,
            isDeleted: Boolean,
            replies: List<CommentSummaryDto>,
        ): CommentSummaryDto {
            return CommentSummaryDto(
                commentId = commentId,
                content = content,
                authorId = authorId,
                authorName = authorName,
                authorProfileImageUrl = authorProfileImageUrl,
                createdAt = createdAt,
                likeCount = likeCount,
                isDeleted = isDeleted,
                replies = replies,
            )
        }
    }
}
