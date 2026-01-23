package kr.co.wground.study.presentation.request.recruitment

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class StudyRecruitRequest(
    @field:NotBlank(message = "자기소개는 필수입니다.")
    @field:Size(max = 1000, message = "자기소개는 1000자 이하여야 합니다.")
    val appeal: String
)