package kr.co.wground.notification.domain.vo

import jakarta.persistence.Embeddable
import kr.co.wground.exception.BusinessException
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

    private fun validateTitle() {
        require(title.isNotBlank()) {
            throw BusinessException(INVALID_NOTIFICATION_INPUT)
        }
    }

    private fun validateContent() {
        require(content.isNotBlank()) {
            throw BusinessException(INVALID_NOTIFICATION_INPUT)
        }
    }
}
