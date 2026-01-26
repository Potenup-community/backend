package kr.co.wground.post.infra

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EventDedupRepository: JpaRepository<ProcessedEventEntity, UUID> {
}
