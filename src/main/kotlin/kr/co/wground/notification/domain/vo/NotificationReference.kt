package kr.co.wground.notification.domain.vo

import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import kr.co.wground.exception.BusinessException
import kr.co.wground.notification.domain.enums.ReferenceType
import kr.co.wground.notification.exception.NotificationErrorCode

@Embeddable
data class NotificationReference(
    @Enumerated(EnumType.STRING)
    val referenceType: ReferenceType,
    val referenceId: Long,
) {
    init {
        if (referenceId <= 0) {
            throw BusinessException(NotificationErrorCode.INVALID_NOTIFICATION_REFERENCE)
        }
    }
}
