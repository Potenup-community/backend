package kr.co.wground.user.controller.dto.request

import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserStatus

data class SignUpRequest(
    val affiliationId: Long,
    var name : String,
    val role: UserRole,
    val phoneNumber: String,
    val provider: String,
)
