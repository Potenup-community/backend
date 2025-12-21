package kr.co.wground.reaction.infra.querydsl

import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.domain.QCommentReaction.commentReaction
import kr.co.wground.reaction.domain.enums.ReactionType
import org.springframework.stereotype.Repository

@Repository
class CommentReactionQuerydslRepository(
    private val queryFactory: JPAQueryFactory
): CustomCommentReactionRepository {

    override fun fetchCommentReactionStatsRows(commentIds: Set<CommentId>, userId: UserId): List<CommentReactionStatsRow> {
        if (commentIds.isEmpty()) {
            return emptyList()
        }

        val reactedByMeFlag = CaseBuilder()
            .`when`(commentReaction.userId.eq(userId)).then(1)
            .otherwise(0)

        val reactedByMeMax = reactedByMeFlag.max()
        val countExpr = commentReaction.id.count()

        val tuples = queryFactory
            /*
             * 쿼리 의도(나중에 까먹을 까봐 여기 적어둠)
             * - 그룹 집계 기준           : commentId, reactionType
             * - 각 그룹의 크기(count)    : qcr.id.count(), id 는 유일한 값이므로 id 를 카운트
             * - reactedByMeFlag.max()  : 각 그룹 내에 userId 가 같은 애가 하나라도 있으면(사실 최대 하나만 있을 수 있긴 함) 표시
             */
            .select(
                commentReaction.commentId,
                commentReaction.reactionType,
                countExpr,
                reactedByMeMax
            )
            .from(commentReaction)
            .where(commentReaction.commentId.`in`(commentIds))
            .groupBy(commentReaction.commentId, commentReaction.reactionType)
            .fetch()

        return tuples.map { tuple ->

            val commentId = tuple.get(commentReaction.commentId)
                ?: throw IllegalStateException("commentId 는 null 일 수 없으나, null 상태입니다.")

            val reactionType = tuple.get(commentReaction.reactionType)
                ?: throw IllegalStateException("reactionType 는 null 일 수 없으나, null 상태입니다.")

            val count = tuple.get(countExpr)
                ?: throw IllegalStateException("count 집계 결과는 null 일 수 없으나, null 상태입니다.")

            val reactedByMe = tuple.get(reactedByMeMax)
                ?: throw IllegalStateException("reactedByMe 집계 결과는 null 일 수 없으나, null 상태입니다.")

            CommentReactionStatsRow(
                commentId = commentId,
                reactionType = reactionType,
                count = count,
                reactedByMe = reactedByMe > 0
            )
        }
    }

    data class CommentReactionStatsRow(
        val commentId: CommentId,
        val reactionType: ReactionType,
        val count: Long,
        val reactedByMe: Boolean
    )
}
