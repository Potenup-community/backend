package kr.co.wground.reaction.infra.querydsl

import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.domain.QPostReaction.postReaction
import kr.co.wground.reaction.domain.enums.ReactionType
import org.springframework.stereotype.Repository

@Repository
class PostReactionQuerydslRepository(
    private val queryFactory: JPAQueryFactory
) {

    fun fetchPostReactionStatsRows(postIds: Set<PostId>, userId: UserId): List<PostReactionStatsRow> {
        if (postIds.isEmpty()) {
            return emptyList()
        }

        val reactedByMeFlag = CaseBuilder()
            .`when`(postReaction.userId.eq(userId)).then(1)
            .otherwise(0)

        val reactedByMeMax = reactedByMeFlag.max()
        val countExpr = postReaction.id.count()

        val tuples = queryFactory
            /*
             * 쿼리 의도(나중에 까먹을 까봐 여기 적어둠)
             * - 그룹 집계 기준           : postId, reactionType
             * - 각 그룹의 크기(count)    : qpr.id.count(), id 는 유일한 값이므로 id 를 카운트
             * - reactedByMeFlag.max()  : 각 그룹 내에 userId 가 같은 애가 하나라도 있으면(사실 최대 하나만 있을 수 있긴 함) 표시
             */
            .select(
                postReaction.postId,
                postReaction.reactionType,
                countExpr,
                reactedByMeMax
            )
            .from(postReaction)
            .where(postReaction.postId.`in`(postIds))
            .groupBy(postReaction.postId, postReaction.reactionType)
            .fetch()

        return tuples.map { tuple ->

            val postId = tuple.get(postReaction.postId)
                ?: throw IllegalStateException("postId 는 null 일 수 없으나, null 상태입니다.")

            val reactionType = tuple.get(postReaction.reactionType)
                ?: throw IllegalStateException("reactionType 는 null 일 수 없으나, null 상태입니다.")

            val count = tuple.get(countExpr)
                ?: throw IllegalStateException("count 집계 결과는 null 일 수 없으나, null 상태입니다.")

            val reactedByMe = tuple.get(reactedByMeMax)
                ?: throw IllegalStateException("reactedByMe 집계 결과는 null 일 수 없으나, null 상태입니다.")

            PostReactionStatsRow(
                postId = postId,
                reactionType = reactionType,
                count = count,
                reactedByMe = reactedByMe > 0
            )
        }
    }

    data class PostReactionStatsRow(
        val postId: PostId,
        val reactionType: ReactionType,
        val count: Long,
        val reactedByMe: Boolean
    )
}