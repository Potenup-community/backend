package kr.co.wground.common.outbox.repository

import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.common.outbox.entity.OutboxEventEntity
import kr.co.wground.common.outbox.entity.OutboxStatus
import kr.co.wground.common.outbox.entity.QOutboxEventEntity.outboxEventEntity
import java.time.Instant

class CustomOutboxEventRepositoryImpl(
    private val queryFactory: JPAQueryFactory
): CustomOutboxEventRepository {
    companion object {
        const val PRIORITY_HIGH = 0
        const val PRIORITY_LOW = 1
    }

    override fun findPublishCandidates(now: Instant, limit: Long): List<OutboxEventEntity> {
        val pending = outboxEventEntity.status.eq(OutboxStatus.PENDING)
        val retryableFailed = outboxEventEntity.status.eq(OutboxStatus.FAILED)
            .and(outboxEventEntity.nextRetryAt.isNull.or(outboxEventEntity.nextRetryAt.loe(now)))

        val priority = CaseBuilder()
            .`when`(outboxEventEntity.status.eq(OutboxStatus.PENDING)).then(PRIORITY_HIGH)
            .otherwise(PRIORITY_LOW)

        return queryFactory
            .selectFrom(outboxEventEntity)
            .where(pending.or(retryableFailed))
            .orderBy(
                priority.asc(),
                outboxEventEntity.nextRetryAt.asc().nullsFirst(),
                outboxEventEntity.createdAt.asc()
            )
            .limit(limit)
            .fetch()
    }
}
