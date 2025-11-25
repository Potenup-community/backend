package kr.co.wground.user.presentation.request

import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserSignupStatus

data class DecisionStatusRequest(
    val id : Long,
    val role : UserRole?,
    val requestStatus : UserSignupStatus
)
