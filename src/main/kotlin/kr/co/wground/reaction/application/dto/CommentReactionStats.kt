package kr.co.wground.reaction.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import kr.co.wground.global.common.CommentId
import kr.co.wground.reaction.domain.enums.ReactionType

@Schema(description = "댓글 리액션 조회 응답 객체")
data class CommentReactionStats(
    val commentId: CommentId,
    val totalCount: Int,
    val summaries: Map<ReactionType, ReactionSummary>
) {
    companion object {
        fun emptyOf(commentId: CommentId) : CommentReactionStats {
            return CommentReactionStats(
                commentId = commentId,
                totalCount = 0,
                summaries = emptyMap()
            )
        }
    }
}