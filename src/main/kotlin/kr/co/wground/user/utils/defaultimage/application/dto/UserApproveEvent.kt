package kr.co.wground.user.utils.defaultimage.application.dto

data class UserApproveEvent(
    val userId: Long,
    val userEmail: String,
    val userName: String,
)
