package kr.co.wground.notification.infra.broadcast

import kr.co.wground.notification.domain.repository.BroadcastNotificationWithReadStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice

interface CustomBroadcastNotificationRepository {
    fun findByTargetWithReadStatus(
        userId: Long,
        trackId: Long?,
        pageable: Pageable
    ): Slice<BroadcastNotificationWithReadStatus>

    fun countUnreadByUserIdAndTrackId(userId: Long, trackId: Long?): Long

    fun findUnreadIdsByUserIdAndTrackId(userId: Long, trackId: Long?): List<Long>
}
