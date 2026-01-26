package kr.co.wground.study.presentation.request.recruitment

import jakarta.validation.constraints.NotNull
import kr.co.wground.study.domain.constant.RecruitStatus

data class RecruitmentStatusUpdateRequest(
    @field:NotNull(message = "신청 상태는 필수입니다.")
    val status: RecruitStatus
)
