package kr.co.wground.notification.domain.vo

import jakarta.persistence.Embeddable
import kr.co.wground.exception.BusinessException
import kr.co.wground.notification.domain.enums.ReferenceType
import kr.co.wground.notification.exception.NotificationErrorCode

@Embeddable
data class NotificationReference(
    val referenceType: ReferenceType,
    val referenceId: Long,
    val subReferenceId: Long? = null,
) {
    init {
        if (referenceId <= 0) {
            throw BusinessException(NotificationErrorCode.INVALID_NOTIFICATION_REFERENCE)
        }
        subReferenceId?.let {
            if (it <= 0) {
                throw BusinessException(NotificationErrorCode.INVALID_NOTIFICATION_REFERENCE)
            }
        }
    }
}
