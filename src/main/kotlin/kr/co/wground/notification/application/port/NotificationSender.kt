package kr.co.wground.notification.application.port

import kr.co.wground.notification.infra.slack.SlackChannel

interface NotificationSender {
    fun send(message: NotificationMessage)
}

data class NotificationMessage(
    val type: NotificationMessageType,
    val channel: SlackChannel = type.defaultChannel,
    val link: String? = null,
    val metadata: Map<String, String> = emptyMap(),
)

enum class NotificationMessageType {
    // 전체 채널 (GENERAL)
    ANNOUNCEMENT,
    NEW_POSTS_SUMMARY,

    // 스터디 채널 (STUDY)
    STUDY_RECRUIT_START_REMINDER,
    STUDY_RECRUIT_END_REMINDER,
}

val NotificationMessageType.defaultChannel: SlackChannel
    get() = when (this) {
        NotificationMessageType.ANNOUNCEMENT,
        NotificationMessageType.NEW_POSTS_SUMMARY -> SlackChannel.GENERAL

        NotificationMessageType.STUDY_RECRUIT_START_REMINDER,
        NotificationMessageType.STUDY_RECRUIT_END_REMINDER -> SlackChannel.STUDY
    }
