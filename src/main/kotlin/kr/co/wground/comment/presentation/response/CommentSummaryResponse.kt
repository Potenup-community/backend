package kr.co.wground.comment.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import kr.co.wground.comment.application.dto.CommentSummaryDto
import kr.co.wground.global.common.CommentId
import kr.co.wground.reaction.application.dto.CommentReactionStats

data class CommentSummaryResponse(
    @field:Schema(description = "댓글 ID")
    val commentId: CommentId,
    @field:Schema(description = "댓글 내용")
    val content: String,
    @field:Schema(description = "댓글 작성자 정보")
    val author: CommentAuthorResponse,
    @field:Schema(description = "댓글 작성 시간")
    val createdAt: LocalDateTime,
    @field:Schema(description = "댓글 반응 통계")
    val commentReactionStats: CommentReactionStats,
    @field:Schema(description = "댓글 삭제 여부")
    val isDeleted: Boolean,
    @field:Schema(description = "대댓글 목록")
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
