package kr.co.wground.notification.application.port

import kr.co.wground.notification.domain.enums.NotificationAudience
import kr.co.wground.notification.domain.enums.SlackChannelCategory

interface NotificationSender {
    fun send(message: NotificationMessage)
}

data class NotificationMessage(
    val type: NotificationMessageType,
    val audience: NotificationAudience = NotificationAudience.ALL,
    val link: String? = null,
    val metadata: Map<String, String> = emptyMap(),
)

enum class NotificationMessageType(
    val slackCategory: SlackChannelCategory,
) {
    // 일반 채널
    ANNOUNCEMENT(SlackChannelCategory.GENERAL),
    NEW_POSTS_SUMMARY(SlackChannelCategory.GENERAL),

    // 스터디 채널
    STUDY_RECRUIT_START_REMINDER(SlackChannelCategory.STUDY),
    STUDY_RECRUIT_END_REMINDER(SlackChannelCategory.STUDY),
}
