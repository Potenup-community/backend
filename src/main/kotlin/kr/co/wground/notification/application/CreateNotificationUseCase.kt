package kr.co.wground.notification.application

import kr.co.wground.common.NotificationCreateEvent

interface CreateNotificationUseCase {
    fun createNotification(event: NotificationCreateEvent)
}
