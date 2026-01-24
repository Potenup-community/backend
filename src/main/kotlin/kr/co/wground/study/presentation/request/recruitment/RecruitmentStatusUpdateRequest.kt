package kr.co.wground.study.presentation.request.recruitment

import kr.co.wground.study.domain.constant.RecruitStatus

data class RecruitmentStatusUpdateRequest(
    val status: RecruitStatus
)
