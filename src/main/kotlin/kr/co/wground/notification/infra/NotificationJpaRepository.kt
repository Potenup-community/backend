package kr.co.wground.notification.infra

import kr.co.wground.global.common.NotificationId
import kr.co.wground.global.common.RecipientId
import kr.co.wground.notification.domain.Notification
import kr.co.wground.notification.domain.enums.NotificationStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface NotificationJpaRepository : JpaRepository<Notification, Long>, CustomNotificationRepository {
    fun existsByEventId(eventId: UUID): Boolean
    fun findByIdAndRecipientId(id: NotificationId, recipientId: RecipientId): Notification?
    fun findByRecipientIdOrderByCreatedAtDesc(recipientId: RecipientId, pageable: Pageable): Slice<Notification>
    fun countByRecipientIdAndStatus(recipientId: RecipientId, status: NotificationStatus): Long
}
