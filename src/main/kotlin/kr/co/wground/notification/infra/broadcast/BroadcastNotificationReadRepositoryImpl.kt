package kr.co.wground.notification.infra.broadcast

import kr.co.wground.notification.domain.BroadcastNotificationRead
import kr.co.wground.notification.domain.repository.BroadcastNotificationReadRepository
import org.springframework.stereotype.Repository

@Repository
class BroadcastNotificationReadRepositoryImpl(
    private val jpaRepository: BroadcastNotificationReadJpaRepository,
) : BroadcastNotificationReadRepository {

    override fun save(read: BroadcastNotificationRead): BroadcastNotificationRead {
        return jpaRepository.save(read)
    }

    override fun saveAll(reads: List<BroadcastNotificationRead>): List<BroadcastNotificationRead> {
        return jpaRepository.saveAll(reads)
    }

    override fun existsByUserIdAndNotificationId(userId: Long, notificationId: Long): Boolean {
        return jpaRepository.existsByUserIdAndNotificationId(userId, notificationId)
    }

    override fun findByUserIdAndNotificationIds(
        userId: Long,
        notificationIds: List<Long>
    ): List<BroadcastNotificationRead> {
        return jpaRepository.findByUserIdAndNotificationIdIn(userId, notificationIds)
    }
}
