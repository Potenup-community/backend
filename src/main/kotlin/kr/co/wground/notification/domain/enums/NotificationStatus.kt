package kr.co.wground.notification.domain.enums

enum class NotificationStatus {
    READ, UNREAD;

    fun isRead(): Boolean = this == READ
}
