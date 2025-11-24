package kr.co.wground.user.presentation.request

import kr.co.wground.user.domain.constant.UserRole

data class SignUpRequest(
    val idToken: String,
    val trackId: Long,
    val name : String,
    val phoneNumber: String,
    val provider: String,
)
