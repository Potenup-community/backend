package kr.co.wground.user.application.requestsign.event

import kr.co.wground.user.domain.User
import kr.co.wground.user.presentation.request.SignUpRequest

data class SignUpEvent(val user: User)

data class UserAddEvent(
    val request: SignUpRequest,
    val email: String
)
