package kr.co.wground.user.presentation.request

import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserSignupStatus
import kr.co.wground.user.domain.constant.UserStatus

data class UserSearchRequest(
    val name: String? = null,
    val email: String? = null,
    val trackId: Long? = null,
    val role: UserRole? = null,
    val status: UserStatus? = null,
    val requestStatus: UserSignupStatus? = null,
    val provider : String? = null,
    val isGraduated: Boolean? = null,
)
