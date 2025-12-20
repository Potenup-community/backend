package kr.co.wground.comment.infra

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.comment.domain.QComment.comment
import kr.co.wground.comment.domain.vo.CommentCount
import kr.co.wground.global.common.PostId

class CommentRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CommentRepositoryCustom {
    override fun countByPostIds(postIds: List<PostId>): List<CommentCount> {
        return queryFactory
            .select(
                Projections.constructor(
                    CommentCount::class.java,
                    comment.postId,
                    comment.id.count().intValue()
                )
            )
            .from(comment)
            .where(comment.postId.`in`(postIds))
            .where(comment.isDeleted.isFalse)
            .groupBy(comment.postId)
            .fetch()
    }
}
