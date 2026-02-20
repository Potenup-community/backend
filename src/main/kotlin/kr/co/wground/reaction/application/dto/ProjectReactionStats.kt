package kr.co.wground.reaction.application.dto

import kr.co.wground.global.common.ProjectId
import kr.co.wground.reaction.domain.enums.ReactionType

data class ProjectReactionStats(
    val projectId: ProjectId,
    val totalCount: Int,
    val summaries: Map<ReactionType, ReactionSummary>,
)
