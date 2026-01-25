package kr.co.wground.notification.presentation.response

import kr.co.wground.global.common.NotificationId
import kr.co.wground.global.common.UserId
import kr.co.wground.notification.application.query.dto.NotificationSummaryDto
import kr.co.wground.notification.domain.enums.NotificationStatus
import kr.co.wground.notification.domain.enums.NotificationType
import kr.co.wground.notification.domain.enums.ReferenceType
import org.springframework.data.domain.Slice
import java.time.LocalDateTime

data class NotificationsResponse(
    val notifications: List<NotificationSummaryResponse>,
    val hasNext: Boolean,
)

data class NotificationSummaryResponse(
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
        fun from(dto: NotificationSummaryDto): NotificationSummaryResponse {
            return NotificationSummaryResponse(
                id = dto.id,
                type = dto.type,
                title = dto.title,
                content = dto.content,
                actorId = dto.actorId,
                referenceType = dto.referenceType,
                referenceId = dto.referenceId,
                status = dto.status,
                createdAt = dto.createdAt,
            )
        }
    }
}

data class UnreadCountResponse(
    val count: Long,
)

fun Slice<NotificationSummaryDto>.toResponse(): NotificationsResponse {
    return NotificationsResponse(
        notifications = this.content.map { NotificationSummaryResponse.from(it) },
        hasNext = this.hasNext(),
    )
}
