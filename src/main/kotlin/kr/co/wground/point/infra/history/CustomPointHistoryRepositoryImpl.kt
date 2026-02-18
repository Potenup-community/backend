package kr.co.wground.point.infra.history

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.ComparableExpressionBase
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.global.common.UserId
import kr.co.wground.point.domain.PointType
import kr.co.wground.point.domain.QPointHistory.pointHistory
import java.time.LocalDateTime
import kr.co.wground.point.application.query.dto.PointHistoryFilter
import kr.co.wground.point.application.query.dto.PointHistoryQueryCondition
import kr.co.wground.point.application.query.dto.PointTypeStatsDto
import kr.co.wground.point.domain.PointHistory
import kr.co.wground.point.domain.PointReferenceType
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl

class CustomPointHistoryRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CustomPointHistoryRepository {

    override fun findUserIdsWithHistory(
        userIds: List<UserId>,
        refType: PointReferenceType,
        refId: Long,
        type: PointType
    ): List<UserId> {
        if (userIds.isEmpty()) return emptyList()

        return queryFactory
            .select(pointHistory.userId)
            .from(pointHistory)
            .where(
                pointHistory.userId.`in`(userIds),
                pointHistory.refType.eq(refType),
                pointHistory.refId.eq(refId),
                pointHistory.type.eq(type)
            )
            .fetch()
    }

    override fun countByUserIdAndTypeInDay(
        userId: UserId,
        types: List<PointType>,
        startOfDay: LocalDateTime,
        endOfDay: LocalDateTime
    ): Long {
        if (types.isEmpty()) return 0L

        return queryFactory
            .select(pointHistory.id.count())
            .from(pointHistory)
            .where(
                pointHistory.userId.eq(userId),
                pointHistory.type.`in`(types),
                pointHistory.createdAt.goe(startOfDay),
                pointHistory.createdAt.lt(endOfDay)
            )
            .fetchOne() ?: 0L
    }

    override fun sumAmountByUserIdAndPeriod(
        userId: UserId,
        start: LocalDateTime,
        end: LocalDateTime,
        earnedOnly: Boolean
    ): Long {
        return queryFactory
            .select(pointHistory.amount.sum().coalesce(0L))
            .from(pointHistory)
            .where(
                pointHistory.userId.eq(userId),
                pointHistory.createdAt.goe(start),
                pointHistory.createdAt.lt(end),
                if (earnedOnly) pointHistory.type.ne(PointType.USE_SHOP) else null
            )
            .fetchOne() ?: 0L
    }

    override fun findStatsByUserIdGroupByType(userId: UserId): List<PointTypeStatsDto> {
        return queryFactory
            .select(
                Projections.constructor(
                    PointTypeStatsDto::class.java,
                    pointHistory.type,
                    pointHistory.id.count(),
                    pointHistory.amount.sum().coalesce(0L)
                )
            )
            .from(pointHistory)
            .where(pointHistory.userId.eq(userId))
            .groupBy(pointHistory.type)
            .fetch()
    }

    override fun countDailyByUserIdAndType(
        userId: UserId,
        type: PointType,
        startOfDay: LocalDateTime,
        endOfDay: LocalDateTime
    ): Long {
        return queryFactory
            .select(pointHistory.id.count())
            .from(pointHistory)
            .where(
                pointHistory.userId.eq(userId),
                pointHistory.type.eq(type),
                pointHistory.createdAt.goe(startOfDay),
                pointHistory.createdAt.lt(endOfDay)
            )
            .fetchOne() ?: 0L
    }

    override fun findHistoryByUserId(condition: PointHistoryQueryCondition): Slice<PointHistory> {
        val resolveCondition = resolveCondition(condition)
        val orderSpecifiers = resolveOrderSpecifiers(condition.pageable)

        return fetchSlice(condition.pageable) {
            queryFactory
                .selectFrom(pointHistory)
                .where(
                    pointHistory.userId.eq(condition.userId),
                    condition.type?.let { pointHistory.type.eq(it) },
                    resolveCondition
                )
                .orderBy(*orderSpecifiers)
        }
    }

    private fun fetchSlice(
        pageable: Pageable,
        queryBuilder: () -> JPAQuery<PointHistory>
    ): Slice<PointHistory> {
        val content = queryBuilder()
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong() + 1)
            .fetch()
            .toList()

        val hasNext = content.size > pageable.pageSize

        return SliceImpl(
            content.take(pageable.pageSize),
            pageable,
            hasNext
        )
    }

    private fun resolveOrderSpecifiers(pageable: Pageable):  Array<OrderSpecifier<*>> {
        if (pageable.sort.isUnsorted) {
            return arrayOf(pointHistory.createdAt.desc())
        }
        return pageable.sort
            .map { order ->
                val path: ComparableExpressionBase<*> = when (order.property) {
                    "createdAt" -> pointHistory.createdAt
                    "amount"    -> pointHistory.amount
                    else        -> pointHistory.createdAt
                }
                if (order.isAscending) path.asc() else path.desc()
            }.toList()
            .toTypedArray()
    }

    fun resolveCondition(condition: PointHistoryQueryCondition): BooleanExpression? {
        return when (condition.filter) {
            PointHistoryFilter.ALL -> null
            PointHistoryFilter.USED -> pointHistory.type.eq(PointType.USE_SHOP)
            PointHistoryFilter.EARNED -> pointHistory.type.ne(PointType.USE_SHOP)
        }
    }
}