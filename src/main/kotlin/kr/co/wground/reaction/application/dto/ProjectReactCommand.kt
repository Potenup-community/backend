package kr.co.wground.reaction.application.dto

import kr.co.wground.global.common.ProjectId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.domain.enums.ReactionType

data class ProjectReactCommand(
    val userId: UserId,
    val projectId: ProjectId,
    val reactionType: ReactionType,
)
