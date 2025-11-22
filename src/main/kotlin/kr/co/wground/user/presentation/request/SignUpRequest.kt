package kr.co.wground.user.presentation.request

import kr.co.wground.user.domain.constant.UserRole

data class SignUpRequest(

    val idToken: String,
    val affiliationId: Long,
    val name : String,
    val role : UserRole,
    val phoneNumber: String,
    val provider: String,

){


}
