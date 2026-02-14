package kr.co.wground.point.infra.history

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.global.common.UserId
import kr.co.wground.point.domain.PointType
import kr.co.wground.point.domain.QPointHistory.pointHistory
import java.time.LocalDateTime
import kr.co.wground.point.application.query.dto.PointTypeStatsDto
import kr.co.wground.point.domain.PointHistory
import kr.co.wground.point.domain.PointReferenceType
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Repository

@Repository
class CustomPointHistoryRepositoryImpl(
    private val queryFactory: JPAQueryFactory
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
                if (earnedOnly) pointHistory.amount.gt(0) else null
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

    override fun findByUserId(userId: UserId, pageable: Pageable): Slice<PointHistory> {
        return fetchSlice(pageable) {
            queryFactory
                .selectFrom(pointHistory)
                .where(pointHistory.userId.eq(userId))
                .orderBy(pointHistory.createdAt.desc())
        }
    }

    override fun findByUserIdAndType(
        userId: UserId,
        type: PointType,
        pageable: Pageable
    ): Slice<PointHistory> {
        return fetchSlice(pageable) {
            queryFactory
                .selectFrom(pointHistory)
                .where(
                    pointHistory.userId.eq(userId),
                    pointHistory.type.eq(type)
                )
                .orderBy(pointHistory.createdAt.desc())
        }
    }

    override fun findEarnedByUserId(userId: UserId, pageable: Pageable): Slice<PointHistory> {
        return fetchSlice(pageable) {
            queryFactory
                .selectFrom(pointHistory)
                .where(
                    pointHistory.userId.eq(userId),
                    pointHistory.amount.gt(0)
                )
                .orderBy(pointHistory.createdAt.desc())
        }
    }

    override fun findUsedByUserId(userId: UserId, pageable: Pageable): Slice<PointHistory> {
        return fetchSlice(pageable) {
            queryFactory
                .selectFrom(pointHistory)
                .where(
                    pointHistory.userId.eq(userId),
                    pointHistory.amount.lt(0)
                )
                .orderBy(pointHistory.createdAt.desc())
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
}