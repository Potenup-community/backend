package kr.co.wground.notification.infra.slack

import kr.co.wground.notification.domain.enums.NotificationAudience
import kr.co.wground.notification.domain.enums.SlackChannelCategory

enum class SlackChannel {
    GENERAL_BE,
    STUDY_BE,
    GENERAL_AI,
    STUDY_AI,
    GENERAL_GAME,
    STUDY_GAME,
}

fun SlackChannelCategory.resolveChannels(audience: NotificationAudience): List<SlackChannel> {
    return when (audience) {
        NotificationAudience.BE -> listOf(resolve(track = NotificationAudience.BE))
        NotificationAudience.AI -> listOf(resolve(track = NotificationAudience.AI))
        NotificationAudience.GAME -> listOf(resolve(track = NotificationAudience.GAME))
        NotificationAudience.ALL -> listOf(
            resolve(track = NotificationAudience.BE),
            resolve(track = NotificationAudience.AI),
            resolve(track = NotificationAudience.GAME),
        )
    }
}

private fun SlackChannelCategory.resolve(track: NotificationAudience): SlackChannel {
    return when (this) {
        SlackChannelCategory.GENERAL -> when (track) {
            NotificationAudience.BE -> SlackChannel.GENERAL_BE
            NotificationAudience.AI -> SlackChannel.GENERAL_AI
            NotificationAudience.GAME -> SlackChannel.GENERAL_GAME
            NotificationAudience.ALL -> error("ALL audience cannot be resolved as a single channel")
        }

        SlackChannelCategory.STUDY -> when (track) {
            NotificationAudience.BE -> SlackChannel.STUDY_BE
            NotificationAudience.AI -> SlackChannel.STUDY_AI
            NotificationAudience.GAME -> SlackChannel.STUDY_GAME
            NotificationAudience.ALL -> error("ALL audience cannot be resolved as a single channel")
        }
    }
}
