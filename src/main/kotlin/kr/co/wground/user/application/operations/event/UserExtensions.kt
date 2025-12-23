package kr.co.wground.user.application.operations.event

import kr.co.wground.global.common.UserId
import kr.co.wground.user.domain.User
import kr.co.wground.user.presentation.request.SignUpRequest

fun SignUpRequest.toUserEntity(email: String): User{
    return User(
        trackId = this.trackId,
        email = email,
        name = this.name,
        phoneNumber = this.phoneNumber,
        provider = this.provider
    )
}

fun User.toReturnUserId() : UserId {
    val userId = this.userId
    return userId
}
