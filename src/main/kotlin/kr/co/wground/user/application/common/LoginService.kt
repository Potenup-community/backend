package kr.co.wground.user.application.common

import kr.co.wground.global.common.UserId
import kr.co.wground.user.presentation.request.RefreshTokenRequest
import kr.co.wground.user.presentation.request.LoginRequest
import kr.co.wground.user.presentation.response.TokenResponse

interface LoginService {
    fun login(loginRequest: LoginRequest): TokenResponse
    fun refreshAccessToken(request: RefreshTokenRequest): TokenResponse
    fun logout(userId: UserId)
}
