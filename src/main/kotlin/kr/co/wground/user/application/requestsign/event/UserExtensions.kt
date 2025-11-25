package kr.co.wground.user.application.requestsign.event

import kr.co.wground.global.common.UserId
import kr.co.wground.user.domain.User
import kr.co.wground.user.presentation.request.SignUpRequest

fun SignUpRequest.toUserEntity(email: String): User{
    return User(
        affiliationId = this.affiliationId,
        email = email,
        name = this.name,
        phoneNumber = this.phoneNumber,
        provider = this.provider,
        role = this.role,
    )
}

fun User.toReturnUserId() : UserId {
    val userId = this.userId

    return userId
}
