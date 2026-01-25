package kr.co.wground.common

import java.time.LocalDateTime
import java.util.UUID
import kr.co.wground.global.common.RecipientId
import kr.co.wground.global.common.UserId
import kr.co.wground.notification.domain.enums.NotificationType
import kr.co.wground.notification.domain.vo.NotificationContent
import kr.co.wground.notification.domain.vo.NotificationReference

data class NotificationCreateEvent(
    val eventId: UUID,
    val recipientId: RecipientId,
    val actorId: UserId?,
    val content: NotificationContent,
    val reference: NotificationReference?,
    val type: NotificationType,
    val expiresAt: LocalDateTime?,
) {

}
