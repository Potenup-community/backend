package kr.co.wground.common

import kr.co.wground.global.common.RecipientId
import kr.co.wground.notification.domain.enums.NotificationType
import kr.co.wground.notification.domain.vo.NotificationContent
import java.time.Instant
import java.util.UUID

data class NotificationCreateEvent(
    val eventId: UUID,
    val recipient: RecipientId,
    val content: NotificationContent,
    val type: NotificationType,
    val expiresAt: Instant?,
) {

}
