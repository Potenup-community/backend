package kr.co.wground.reaction.application.dto

import kr.co.wground.global.common.CommentId
import kr.co.wground.reaction.domain.enums.ReactionType

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