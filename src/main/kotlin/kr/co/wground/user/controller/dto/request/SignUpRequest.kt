package kr.co.wground.user.controller.dto.request

import kr.co.wground.user.domain.constant.UserRole

data class SignUpRequest(

    val idToken: String, // email 필드 대신 idToken을 받습니다.
    val affiliationId: Long,
    val name : String,
    val role : UserRole,
    val phoneNumber: String,
    val provider: String,

){


}
