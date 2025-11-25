package kr.co.wground.user.application.login

import kr.co.wground.user.presentation.request.RefreshTokenRequest
import kr.co.wground.user.presentation.request.LoginRequest
import kr.co.wground.user.presentation.response.AccessTokenResponse
import kr.co.wground.user.presentation.response.LoginResponse

interface LoginService {
    fun login(loginRequest: LoginRequest): LoginResponse
    fun refreshAccessToken(request: RefreshTokenRequest) : AccessTokenResponse
}
