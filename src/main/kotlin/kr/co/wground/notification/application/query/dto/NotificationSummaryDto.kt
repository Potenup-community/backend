package kr.co.wground.notification.application.query.dto

import kr.co.wground.global.common.NotificationId
import kr.co.wground.global.common.UserId
import kr.co.wground.notification.domain.Notification
import kr.co.wground.notification.domain.enums.NotificationStatus
import kr.co.wground.notification.domain.enums.NotificationType
import kr.co.wground.notification.domain.enums.ReferenceType
import java.time.LocalDateTime

data class NotificationSummaryDto(
    val id: NotificationId,
    val type: NotificationType,
    val title: String,
    val content: String,
    val actorId: UserId?,
    val referenceType: ReferenceType?,
    val referenceId: Long?,
    val status: NotificationStatus,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(notification: Notification): NotificationSummaryDto {
            return NotificationSummaryDto(
                id = notification.id,
                type = notification.type,
                title = notification.content.title,
                content = notification.content.content,
                actorId = notification.actorId,
                referenceType = notification.reference?.referenceType,
                referenceId = notification.reference?.referenceId,
                status = notification.status,
                createdAt = notification.createdAt,
            )
        }
    }
}
