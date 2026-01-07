package kr.co.wground.user.presentation.request

import kr.co.wground.global.common.UserId
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserSignupStatus

data class MultipleDecisionRequest(
    val ids: List<UserId>,
    val requestStatus: UserSignupStatus,
    val role: UserRole?
)
