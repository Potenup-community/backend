package kr.co.wground.user.presentation.request

import kr.co.wground.user.domain.constant.UserSignupStatus

data class DecisionStatusRequest(
    val id : Long,
    val requestStatus : UserSignupStatus
)
