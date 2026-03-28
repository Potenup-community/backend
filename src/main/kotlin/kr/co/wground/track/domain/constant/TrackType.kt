package kr.co.wground.track.domain.constant

enum class TrackType(val displayName: String) {
    BE("BE"),
    FE("FE"),
    AI("AI Agent"),
    UNREAL("언리얼"),
    GAME("게임"),
    ADMIN("운영자"),
}

fun TrackType?.toDisplayName(cardinal: Int?, defaultValue: String = ""): String {
    val trackType = this ?: return defaultValue
    if (trackType == TrackType.ADMIN || cardinal == null) {
        return trackType.displayName
    }
    return "${trackType.displayName} ${cardinal}기"
}
