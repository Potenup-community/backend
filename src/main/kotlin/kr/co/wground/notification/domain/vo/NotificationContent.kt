package kr.co.wground.notification.domain.vo

import jakarta.persistence.Embeddable
import kr.co.wground.exception.BusinessException
import kr.co.wground.notification.domain.enums.NotificationMessageVariant
import kr.co.wground.notification.domain.enums.NotificationType
import kr.co.wground.notification.exception.NotificationErrorCode.INVALID_NOTIFICATION_INPUT

@Embeddable
data class NotificationContent(
    val title: String,
    val content: String,
) {
    init {
        validateTitle()
        validateContent()
    }

    companion object {
        fun random(
            type: NotificationType,
            title: String,
            placeholders: Map<String, String> = emptyMap()
        ): NotificationContent {
            val message =
                if (placeholders.isEmpty()) {
                    NotificationMessageVariant.getRandomMessage(type)
                } else {
                    NotificationMessageVariant.getRandomMessage(type, placeholders)
                }

            return NotificationContent(
                title = title,
                content = message
            )
        }
    }

    private fun validateTitle() {
        if (title.isBlank()) {
            throw BusinessException(INVALID_NOTIFICATION_INPUT)
        }
    }

    private fun validateContent() {
        if (content.isBlank()) {
            throw BusinessException(INVALID_NOTIFICATION_INPUT)
        }
    }
}
