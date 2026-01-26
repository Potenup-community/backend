package kr.co.wground.notification.domain.repository

import kr.co.wground.global.common.NotificationId
import kr.co.wground.global.common.RecipientId
import kr.co.wground.notification.domain.Notification
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import java.util.UUID

interface NotificationRepository {
    fun save(notification: Notification): Notification
    fun findByEventId(eventId: UUID): Boolean
    fun findByIdAndRecipientId(id: NotificationId, recipientId: RecipientId): Notification?
    fun findByRecipientId(recipientId: RecipientId, pageable: Pageable): Slice<Notification>
    fun countUnreadByRecipientId(recipientId: RecipientId): Long
    fun markAllAsReadByRecipientId(recipientId: RecipientId): Long
}
