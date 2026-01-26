package kr.co.wground.post.infra

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.LocalDateTime
import java.util.UUID

@Entity
class ProcessedEventEntity(
    @Id
    val eventId: UUID,
    val occurredAt: LocalDateTime = LocalDateTime.now(),
)
