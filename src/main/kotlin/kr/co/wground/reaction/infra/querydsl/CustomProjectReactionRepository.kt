package kr.co.wground.reaction.infra.querydsl

import kr.co.wground.global.common.ProjectId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.infra.querydsl.CustomProjectReactionRepositoryImpl.ProjectReactionStatsRow

interface CustomProjectReactionRepository {
    fun fetchProjectReactionStatsRows(projectIds: Set<ProjectId>, userId: UserId): List<ProjectReactionStatsRow>
}
