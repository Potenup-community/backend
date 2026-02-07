package kr.co.wground.notification.infra.broadcast

import kr.co.wground.notification.domain.BroadcastNotification
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BroadcastNotificationJpaRepository : JpaRepository<BroadcastNotification, Long>,
    CustomBroadcastNotificationRepository {
    fun findByEventId(eventId: UUID): BroadcastNotification?
}
