package kr.co.wground.notification.application.listener

import kr.co.wground.exception.BusinessException
import kr.co.wground.notification.exception.NotificationErrorCode

internal const val NOTIFICATION_EXECUTOR = "notificationExecutor"

internal fun createNotificationSafely(action: () -> Unit) {
    try {
        action()
    } catch (e: BusinessException) {
        if (e.code == NotificationErrorCode.DUPLICATE_NOTIFICATION.code) {
            return
        }
        throw e
    }
}
