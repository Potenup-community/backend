package kr.co.wground.user.controller.dto.request

import kr.co.wground.user.domain.User
import kr.co.wground.user.domain.constant.UserRole

data class SignUpRequest(

    val affiliationId: Long,
    val email: String,
    val name : String,
    val role : UserRole,
    val phoneNumber: String,
    val provider: String,
){
    fun toUser(): User{
        return User(
            userId = null,
            affiliationId = affiliationId,
            name = name,
            role = role,
            email = email,
            phoneNumber = phoneNumber,
            provider = provider
        )
    }
}
