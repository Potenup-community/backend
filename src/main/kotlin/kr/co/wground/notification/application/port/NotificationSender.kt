package kr.co.wground.notification.application.port

interface NotificationSender {
    fun send(message: NotificationMessage)
}

data class NotificationMessage(
    val type: NotificationMessageType,
    val title: String,
    val content: String,
    val link: String? = null,
    val metadata: Map<String, Any> = emptyMap(),
)

enum class NotificationMessageType {
    ANNOUNCEMENT,
    NEW_POSTS_SUMMARY,
}
