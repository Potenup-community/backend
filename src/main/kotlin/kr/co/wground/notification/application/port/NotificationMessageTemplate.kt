package kr.co.wground.notification.application.port

interface NotificationMessageTemplate<T> {
    fun getTemplate(type: NotificationMessageType): T
    fun format(type: NotificationMessageType, params: Map<String, String>): T
}
