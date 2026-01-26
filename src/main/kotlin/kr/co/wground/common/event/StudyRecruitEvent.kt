package kr.co.wground.common.event

import kr.co.wground.global.common.WriterId

data class StudyRecruitEvent(
    val studyId: Long,
    val studyLeaderId: WriterId,
)
