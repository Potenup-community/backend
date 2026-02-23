package kr.co.wground.common.outbox.repository

import kr.co.wground.common.outbox.entity.OutboxEventEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OutboxEventRepository:
    JpaRepository<OutboxEventEntity, UUID>,
    CustomOutboxEventRepository
