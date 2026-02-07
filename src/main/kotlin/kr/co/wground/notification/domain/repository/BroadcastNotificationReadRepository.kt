package kr.co.wground.notification.domain.repository

import kr.co.wground.notification.domain.BroadcastNotificationRead

interface BroadcastNotificationReadRepository {
    fun save(read: BroadcastNotificationRead): BroadcastNotificationRead

    fun saveAll(reads: List<BroadcastNotificationRead>): List<BroadcastNotificationRead>

    fun existsByUserIdAndNotificationId(userId: Long, notificationId: Long): Boolean

    fun findByUserIdAndNotificationIds(
        userId: Long,
        notificationIds: List<Long>
    ): List<BroadcastNotificationRead>
}
