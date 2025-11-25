package kr.co.wground.user.application.login

import kr.co.wground.like.domain.UserId
import kr.co.wground.user.presentation.request.RefreshTokenRequest
import kr.co.wground.user.presentation.request.LoginRequest
import kr.co.wground.user.presentation.response.AccessTokenResponse
import kr.co.wground.user.presentation.response.LoginResponse
import kr.co.wground.user.presentation.response.UserInfoResponse

interface LoginService {
    fun login(loginRequest: LoginRequest): LoginResponse
    fun refreshAccessToken(request: RefreshTokenRequest) : AccessTokenResponse
    fun userInfo(userId : UserId?) : UserInfoResponse
}
