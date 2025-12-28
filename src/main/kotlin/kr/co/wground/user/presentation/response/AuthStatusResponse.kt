package kr.co.wground.user.presentation.response

import kr.co.wground.global.common.UserId

data class AuthStatusResponse(
    val isAuthenticated: Boolean,
    val userId: UserId?,
    val role: String?
)
