package kr.co.wground.post.infra

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.NumberExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.global.common.UserId
import kr.co.wground.post.domain.Post
import kr.co.wground.post.domain.QPost.post
import kr.co.wground.post.domain.enums.Topic
import kr.co.wground.post.infra.dto.PostNavigationDto
import kr.co.wground.post.infra.predicate.GetPostSummaryPredicate
import org.springframework.data.domain.SliceImpl
import org.springframework.data.domain.Sort
import java.time.LocalDateTime

class CustomPostRepositoryImpl(private val jpaQueryFactory: JPAQueryFactory): CustomPostRepository {
    private companion object {
        const val TIME_PASS_AFTER_POST_CREATED = "CAST(GREATEST(0, 1e0 * TIMESTAMPDIFF(HOUR, {0}, NOW())) AS DOUBLE)"
        const val LN_ONE_PLUS = "CAST(LN(1 + {0}) AS DOUBLE)"
        const val POW_WITH_OFFSET = "CAST(POW(({0} + {1}), {2}) AS DOUBLE)"
        const val CREATED_AT = "createdAt"
        const val POPULARITY = "popularity"
    }

    override fun findAllByPredicate(predicate: GetPostSummaryPredicate): SliceImpl<Post> {
        val pageable = predicate.pageable

        val content = jpaQueryFactory.selectFrom(post)
            .where(
                eqTopic(predicate.topic),
                eqUserId(predicate.userId)
            )
            .orderBy(
                *toOrderSpecifiers(pageable.sort).toTypedArray(),
                post.id.desc()
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong() + 1)
            .fetch()

        val hasNext = content.size > pageable.pageSize

        return SliceImpl(
            content.take(pageable.pageSize),
            pageable,
            hasNext
        )
    }

    override fun findIdsOfPreviousAndNext(
        currentPostId: Long,
        currentCreatedAt: LocalDateTime
    ): PostNavigationDto {
        val previousPostId = jpaQueryFactory
            .select(post.id)
            .from(post)
            .where(
                post.createdAt.gt(currentCreatedAt)
                    .or(
                        post.createdAt.eq(currentCreatedAt)
                            .and(post.id.gt(currentPostId))
                    )
            )
            .orderBy(post.createdAt.asc(), post.id.asc())
            .fetchFirst()

        val nextPostId = jpaQueryFactory
            .select(post.id)
            .from(post)
            .where(
                post.createdAt.lt(currentCreatedAt)
                    .or(
                        post.createdAt.eq(currentCreatedAt)
                            .and(post.id.lt(currentPostId))
                    )
            )
            .orderBy(post.createdAt.desc(), post.id.desc())
            .fetchFirst()

        return PostNavigationDto(previousPostId, nextPostId)
    }

    private data class PopularityParams(
        val a: Double = 3.0,
        val c: Double = 2.0,
        val alpha: Double = 1.5
    )

    fun popularityExpr(): NumberExpression<Double> {
        val params = PopularityParams()

        // R: reaction 총합
        val reaction = post.reactionCount.doubleValue()

        // V: recent view count
        val viewCount = post.recentViewCount.doubleValue()

        // 게시글이 만들어진지 지난 시간 (단위: 시간)
        val tHours: NumberExpression<Double> =
            Expressions.numberTemplate(
                Double::class.java,
                TIME_PASS_AFTER_POST_CREATED,
                post.createdAt
            )

        val ln1pViewCount: NumberExpression<Double> =
            Expressions.numberTemplate(
                Double::class.java,
                LN_ONE_PLUS,
                viewCount
            )

        // numerator = 리액션 + (조회수 신호 × 가중치) -> 후항은 이후 조회수가 추가될 시 추가
        val numerator: NumberExpression<Double> =
            reaction.add(ln1pViewCount.multiply(params.a))

        // denominator = POW(경과시 + 초기 완충, 시간 감쇠강도)
        val denominator: NumberExpression<Double> =
            Expressions.numberTemplate(
                Double::class.java,
                POW_WITH_OFFSET,
                tHours,
                params.c,
                params.alpha
            )

        // popularity = numerator / denominator
        return numerator.divide(denominator)
    }

    private fun toOrderSpecifiers(sort: Sort): List<OrderSpecifier<*>> {
        val result = mutableListOf<OrderSpecifier<*>>()

        for (o in sort) {
            val spec: OrderSpecifier<*>? = when (o.property) {
                CREATED_AT -> if (o.isAscending) post.createdAt.asc() else post.createdAt.desc()
                POPULARITY -> {
                    val expr = popularityExpr()
                    if (o.isAscending) expr.asc() else expr.desc()
                }

                else -> null
            }
            spec?.let { result += spec }
        }

        result += post.id.desc()
        return result
    }

    private fun eqTopic(topic: Topic?) = topic?.let { post.topic.eq(it) }
    private fun eqUserId(userId: UserId?) = userId?.let { post.writerId.eq(it) }
}
