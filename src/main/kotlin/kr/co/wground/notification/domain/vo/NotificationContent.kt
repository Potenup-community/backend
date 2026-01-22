package kr.co.wground.notification.domain.vo

import jakarta.persistence.Embeddable
import kr.co.wground.exception.BusinessException
import kr.co.wground.notification.exception.NotificationErrorCode.INVALID_NOTIFICATION_INPUT

@Embeddable
data class NotificationContent(
    val title: String,
    val content: String,
    val link: String? = null,
) {
    init {
        if (title.isBlank()) {
            throw BusinessException(INVALID_NOTIFICATION_INPUT)
        }
        if (content.isBlank()) {
            throw BusinessException(INVALID_NOTIFICATION_INPUT)
        }
    }
}
