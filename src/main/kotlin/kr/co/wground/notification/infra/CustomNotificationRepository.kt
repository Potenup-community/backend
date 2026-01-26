package kr.co.wground.notification.infra

import kr.co.wground.global.common.RecipientId

interface CustomNotificationRepository {
    fun markAllAsReadByRecipientId(recipientId: RecipientId): Long
}
