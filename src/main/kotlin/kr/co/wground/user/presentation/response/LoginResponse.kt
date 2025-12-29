package kr.co.wground.user.presentation.response

import kr.co.wground.global.common.UserId
import kr.co.wground.user.domain.constant.UserRole

data class LoginResponse(
    val role: UserRole,
    val accessToken: String,
    val refreshToken: String,
)

data class TokenResponse(
    val userId: UserId,
    val userRole: UserRole,
    val accessToken: String,
    val refreshToken: String
)

data class RoleResponse(
    val role: UserRole
)
