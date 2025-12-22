package kr.co.wground.user.utils.defaultimage.application.constant

import kotlin.math.abs

enum class AvatarPalette(val colors: List<String>) {
    BRAND(listOf("#4F46E5", "#818CF8", "#C7D2FE", "#312E81", "#E0E7FF")),
    VIBRANT(listOf("#F87171", "#FBBF24", "#34D399", "#60A5FA", "#A78BFA")),
    MINIMAL(listOf("#94A3B8", "#64748B", "#CBD5E1", "#475569", "#1E293B"));

    companion object {
        fun fromHash(hash: Long): AvatarPalette {
            val index = (abs(hash) % entries.size).toInt()
            return entries[index]
        }
    }
}
