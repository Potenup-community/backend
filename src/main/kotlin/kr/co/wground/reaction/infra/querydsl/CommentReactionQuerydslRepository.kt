package kr.co.wground.reaction.infra.querydsl

import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.domain.QCommentReaction
import kr.co.wground.reaction.domain.enums.ReactionType
import org.springframework.stereotype.Repository

@Repository
class CommentReactionQuerydslRepository(
    private val queryFactory: JPAQueryFactory
) {

    fun fetchCommentReactionStatsRows(commentIds: Set<CommentId>, userId: UserId): List<CommentReactionStatsRow> {
        if (commentIds.isEmpty()) {
            return emptyList()
        }

        val qcr = QCommentReaction.commentReaction

        val reactedByMeFlag = CaseBuilder()
            .`when`(qcr.userId.eq(userId)).then(1)
            .otherwise(0)

        val tuples = queryFactory
            /*
             * 쿼리 의도(나중에 까먹을 까봐 여기 적어둠)
             * - 그룹 집계 기준           : commentId, reactionType
             * - 각 그룹의 크기(count)    : qcr.id.count(), id 는 유일한 값이므로 id 를 카운트
             * - reactedByMeFlag.max()  : 각 그룹 내에 userId 가 같은 애가 하나라도 있으면(사실 최대 하나만 있을 수 있긴 함) 표시
             */
            .select(
                qcr.commentId,
                qcr.reactionType,
                qcr.id.count(),
                reactedByMeFlag.max()
            )
            .from(qcr)
            .where(qcr.commentId.`in`(commentIds))
            .groupBy(qcr.commentId, qcr.reactionType)
            .fetch()

        return tuples.map { tuple ->
            CommentReactionStatsRow(
                commentId = tuple.get(qcr.commentId)!!,
                reactionType = tuple.get(qcr.reactionType)!!,
                count = tuple.get(qcr.id.count())!!,
                reactedByMe = (tuple.get(reactedByMeFlag.max()) ?: 0) > 0
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