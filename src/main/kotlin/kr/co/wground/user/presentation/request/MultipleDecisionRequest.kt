package kr.co.wground.user.presentation.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.co.wground.global.common.UserId
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserSignupStatus

@Schema(description = "가입 요청 승인/거절 데이터 (일괄)")
data class MultipleDecisionRequest(
    @field:Schema(description = "대상 유저 ID 목록", example = "[1, 2, 3]")
    val ids: List<UserId>,

    @field:Schema(description = "처리 상태 (ACCEPTED, REJECTED)", example = "ACCEPTED")
    val requestStatus: UserSignupStatus,

    @field:Schema(description = "부여할 권한 (승인 시 필수)", example = "USER")
    val role: UserRole?
)
