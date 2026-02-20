package kr.co.wground.reaction.infra.querydsl

import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.global.common.ProjectId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.domain.QProjectReaction.projectReaction
import kr.co.wground.reaction.domain.enums.ReactionType
import org.springframework.stereotype.Repository

@Repository
class CustomProjectReactionRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CustomProjectReactionRepository {

    override fun fetchProjectReactionStatsRows(
        projectIds: Set<ProjectId>,
        userId: UserId,
    ): List<ProjectReactionStatsRow> {
        if (projectIds.isEmpty()) return emptyList()

        val reactedByMeFlag = CaseBuilder()
            .`when`(projectReaction.userId.eq(userId)).then(1)
            .otherwise(0)

        val reactedByMeMax = reactedByMeFlag.max()
        val countExpr = projectReaction.id.count()

        /*
         * 그룹 집계 기준          : projectId, reactionType
         * 각 그룹의 크기(count)   : id.count() — id는 유일하므로 행 수와 동일
         * reactedByMeFlag.max() : 그룹 내 로그인 유저 행이 하나라도 있으면 1, 없으면 0
         */
        return queryFactory
            .select(projectReaction.projectId, projectReaction.reactionType, countExpr, reactedByMeMax)
            .from(projectReaction)
            .where(projectReaction.projectId.`in`(projectIds))
            .groupBy(projectReaction.projectId, projectReaction.reactionType)
            .fetch()
            .map { tuple ->
                ProjectReactionStatsRow(
                    projectId = requireNotNull(tuple.get(projectReaction.projectId)),
                    reactionType = requireNotNull(tuple.get(projectReaction.reactionType)),
                    count = requireNotNull(tuple.get(countExpr)),
                    reactedByMe = requireNotNull(tuple.get(reactedByMeMax)) > 0,
                )
            }
    }

    data class ProjectReactionStatsRow(
        val projectId: ProjectId,
        val reactionType: ReactionType,
        val count: Long,
        val reactedByMe: Boolean,
    )
}
