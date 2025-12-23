package kr.co.wground.user.utils.defaultimage.application.event

import kr.co.wground.global.common.UserId

data class UserProfileEvent(
    val userId: UserId,
    val userEmail: String,
    val userName: String,
)
