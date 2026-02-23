package kr.co.wground.common.outbox.repository

import kr.co.wground.common.outbox.entity.OutboxEventEntity
import java.time.Instant

interface CustomOutboxEventRepository {
    fun findPublishCandidates(now: Instant, limit: Long): List<OutboxEventEntity>
}
