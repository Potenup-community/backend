package kr.co.wground.common.event

import kr.co.wground.global.common.UserId

data class StudyRecruitmentEvent(
    val studyId: Long,
    val userId: UserId
)
