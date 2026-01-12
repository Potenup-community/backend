package kr.co.wground.notification.infra

import kr.co.wground.notification.domain.Notification
import kr.co.wground.notification.domain.repository.NotificationRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class NotificationRepositoryImpl(
    private val jpaRepository: NotificationJpaRepository
): NotificationRepository {
    override fun save(notification: Notification): Notification {
        return jpaRepository.save(notification)
    }

    override fun findByEventId(eventId: UUID): Boolean {
        return jpaRepository.existsByEventId(eventId)
    }
}
