package kr.co.wground.comment.presentation.response

import java.time.LocalDateTime
import kr.co.wground.comment.application.dto.CommentSummaryDto
import kr.co.wground.global.common.CommentId

data class CommentSummaryResponse(
    val commentId: CommentId,
    val content: String,
    val author: CommentAuthorResponse,
    val createdAt: LocalDateTime,
    val reactionCount: Int,
    val reactionByMe: Boolean,
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
                    profileImageUrl = dto.authorProfileImageUrl,
                ),
                createdAt = dto.createdAt,
                reactionCount = dto.reactionCount,
                reactionByMe = dto.reactionByMe,
                isDeleted = dto.isDeleted,
                replies = dto.replies.map { from(it) },
            )
        }
    }
}
