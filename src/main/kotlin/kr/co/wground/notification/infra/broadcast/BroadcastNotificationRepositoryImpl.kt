package kr.co.wground.notification.infra.broadcast

import kr.co.wground.notification.domain.BroadcastNotification
import kr.co.wground.notification.domain.repository.BroadcastNotificationRepository
import kr.co.wground.notification.domain.repository.BroadcastNotificationWithReadStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class BroadcastNotificationRepositoryImpl(
    private val jpaRepository: BroadcastNotificationJpaRepository,
) : BroadcastNotificationRepository {

    override fun save(notification: BroadcastNotification): BroadcastNotification {
        return jpaRepository.save(notification)
    }

    override fun findByEventId(eventId: UUID): BroadcastNotification? {
        return jpaRepository.findByEventId(eventId)
    }

    override fun findByTargetWithReadStatus(
        userId: Long,
        trackId: Long?,
        pageable: Pageable
    ): Slice<BroadcastNotificationWithReadStatus> {
        return jpaRepository.findByTargetWithReadStatus(userId, trackId, pageable)
    }

    override fun countUnreadByUserIdAndTrackId(userId: Long, trackId: Long?): Long {
        return jpaRepository.countUnreadByUserIdAndTrackId(userId, trackId)
    }
}
