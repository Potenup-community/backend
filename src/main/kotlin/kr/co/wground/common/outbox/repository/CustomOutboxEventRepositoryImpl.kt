package kr.co.wground.common.outbox.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.common.outbox.QOutboxEventEntity.outboxEventEntity
import kr.co.wground.common.outbox.entity.OutboxEventEntity
import kr.co.wground.common.outbox.entity.OutboxStatus
import java.time.Instant

class CustomOutboxEventRepositoryImpl(
    private val queryFactory: JPAQueryFactory
): CustomOutboxEventRepository {
    override fun findPublishCandidates(now: Instant, limit: Long): List<OutboxEventEntity> {
        val pending = outboxEventEntity.status.eq(OutboxStatus.PENDING)
        val retryableFailed = outboxEventEntity.status.eq(OutboxStatus.FAILED)
            .and(outboxEventEntity.nextRetryAt.isNull.or(outboxEventEntity.nextRetryAt.loe(now)))

        return queryFactory
            .selectFrom(outboxEventEntity)
            .where(pending.or(retryableFailed))
            .orderBy(outboxEventEntity.createdAt.asc())
            .limit(limit)
            .fetch()
    }
}
