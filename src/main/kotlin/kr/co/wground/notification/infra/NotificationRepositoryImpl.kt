package kr.co.wground.notification.infra

import java.util.UUID
import kr.co.wground.global.common.NotificationId
import kr.co.wground.global.common.RecipientId
import kr.co.wground.notification.domain.Notification
import kr.co.wground.notification.domain.enums.NotificationStatus
import kr.co.wground.notification.domain.repository.NotificationRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Repository

@Repository
class NotificationRepositoryImpl(
    private val jpaRepository: NotificationJpaRepository,
) : NotificationRepository {
    override fun save(notification: Notification): Notification {
        return jpaRepository.save(notification)
    }

    override fun findByEventId(eventId: UUID): Boolean {
        return jpaRepository.existsByEventId(eventId)
    }

    override fun findByIdAndRecipientId(id: NotificationId, recipientId: RecipientId): Notification? {
        return jpaRepository.findByIdAndRecipientId(id, recipientId)
    }

    override fun findByRecipientId(recipientId: RecipientId, pageable: Pageable): Slice<Notification> {
        return jpaRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable)
    }

    override fun countUnreadByRecipientId(recipientId: RecipientId): Long {
        return jpaRepository.countByRecipientIdAndStatus(recipientId, NotificationStatus.UNREAD)
    }

    override fun markAllAsReadByRecipientId(recipientId: RecipientId): Long {
        return jpaRepository.markAllAsReadByRecipientId(recipientId)
    }
}
