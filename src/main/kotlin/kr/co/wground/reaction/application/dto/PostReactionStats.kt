package kr.co.wground.reaction.application.dto

import kr.co.wground.global.common.PostId
import kr.co.wground.reaction.domain.enums.ReactionType

data class PostReactionStats(
    val postId: PostId,
    val totalCount: Int,
    val summaries: Map<ReactionType, ReactionSummary>
)