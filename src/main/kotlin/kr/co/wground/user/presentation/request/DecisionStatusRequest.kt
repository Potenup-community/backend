package kr.co.wground.user.presentation.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserSignupStatus

@Schema(description = "가입 요청 승인/거절 데이터 (단건)")
data class DecisionStatusRequest(
    @field:Schema(description = "부여할 권한 (승인 시 필수)", example = "USER")
    val role : UserRole?,

    @field:Schema(description = "처리 상태 (ACCEPTED, REJECTED)", example = "ACCEPTED")
    @field:NotNull(message = "처리 상태값은 필수입니다.")
    val requestStatus : UserSignupStatus
)
