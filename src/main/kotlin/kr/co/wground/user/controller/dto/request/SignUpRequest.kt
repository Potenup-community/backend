package kr.co.wground.user.controller.dto.request

import kr.co.wground.user.domain.constant.UserStatus

data class SignUpRequest(
    val accessToken: String,
    val affiliationId: Long,
    val email: String,
    val name : String,
    val role: String? = null,
    val phoneNumber: String,
    val provider: String? = null,
    val status : UserStatus
)