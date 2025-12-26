package kr.co.wground.user.presentation.request

import jakarta.validation.constraints.NotNull
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserSignupStatus

data class DecisionStatusRequest(
    val role : UserRole?,
    @field:NotNull(message = "처리 상태값은 필수입니다.")
    val requestStatus : UserSignupStatus
)
