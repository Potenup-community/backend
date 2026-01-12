package kr.co.wground.notification.domain.repository

import kr.co.wground.notification.domain.Notification
import java.util.UUID

interface NotificationRepository {
    fun save(notification: Notification): Notification
    fun findByEventId(eventId: UUID): Boolean
}
