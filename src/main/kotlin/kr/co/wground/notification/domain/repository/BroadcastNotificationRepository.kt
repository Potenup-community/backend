package kr.co.wground.notification.domain.repository

import kr.co.wground.notification.domain.BroadcastNotification
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import java.util.UUID

interface BroadcastNotificationRepository {
    fun save(notification: BroadcastNotification): BroadcastNotification

    fun findByEventId(eventId: UUID): BroadcastNotification?

    fun findByTargetWithReadStatus(
        userId: Long,
        trackId: Long?,
        pageable: Pageable
    ): Slice<BroadcastNotificationWithReadStatus>

    fun countUnreadByUserIdAndTrackId(userId: Long, trackId: Long?): Long

    fun findUnreadIdsByUserIdAndTrackId(userId: Long, trackId: Long?): List<Long>
}

data class BroadcastNotificationWithReadStatus(
    val notification: BroadcastNotification,
    val isRead: Boolean,
)
