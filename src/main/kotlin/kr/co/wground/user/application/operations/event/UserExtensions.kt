package kr.co.wground.user.application.operations.event

import kr.co.wground.user.domain.User
import kr.co.wground.user.presentation.request.SignUpRequest

private val NUMERIC_ONLY = Regex("[^0-9]")

fun SignUpRequest.toUserEntity(email: String): User{
    return User(
        trackId = this.trackId,
        email = email,
        name = this.name,
        phoneNumber = this.phoneNumber.replace(NUMERIC_ONLY, ""),
        provider = this.provider
    )
}
