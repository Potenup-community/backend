package kr.co.wground.notification.domain.enums

enum class NotificationAudience {
    BE,
    AI,
    GAME,
    ALL,
    ;

    companion object {
        fun fromTrackName(trackName: String): NotificationAudience? {
            val normalized = trackName.uppercase()
            return when {
                normalized.contains("BE") || trackName.contains("FE") -> BE
                normalized.contains("AI") || trackName.contains("인공지능") -> AI
                normalized.contains("GAME") || trackName.contains("게임") -> GAME
                else -> null
            }
        }
    }
}
