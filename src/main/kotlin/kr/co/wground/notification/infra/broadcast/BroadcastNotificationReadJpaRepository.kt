package kr.co.wground.notification.infra.broadcast

import kr.co.wground.notification.domain.BroadcastNotificationRead
import org.springframework.data.jpa.repository.JpaRepository

interface BroadcastNotificationReadJpaRepository : JpaRepository<BroadcastNotificationRead, Long> {
    fun existsByUserIdAndNotificationId(userId: Long, notificationId: Long): Boolean
    fun findByUserIdAndNotificationIdIn(userId: Long, notificationIds: List<Long>): List<BroadcastNotificationRead>
}
