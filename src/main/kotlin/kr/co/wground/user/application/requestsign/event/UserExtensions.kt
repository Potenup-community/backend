package kr.co.wground.user.application.requestsign.event

import kr.co.wground.exception.BusinessException
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.domain.User
import kr.co.wground.user.presentation.request.SignUpRequest

fun SignUpRequest.toUserEntity(email: String): User{
    return User(
        userId = null,
        affiliationId = this.affiliationId,
        email = email,
        name = this.name,
        phoneNumber = this.phoneNumber,
        provider = this.provider,
        role = this.role,
    )
}

fun User.toReturnUserId() : Long {
    val userId = this.userId ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

    return userId
}
