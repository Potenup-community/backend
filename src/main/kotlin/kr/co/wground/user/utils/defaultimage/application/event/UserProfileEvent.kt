package kr.co.wground.user.utils.defaultimage.application.event

data class UserProfileEvent(
    val userId: Long,
    val userEmail: String,
    val userName: String,
)