package kr.co.wground.user.controller.dto.request

import kr.co.wground.user.domain.constant.UserSignupStatus

data class DecisionStatusRequest(
    val userId: Long,
    val requestStatus : UserSignupStatus
)
