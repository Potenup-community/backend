package kr.co.wground.common.event

import kr.co.wground.global.common.WriterId

data class StudyAppliedEvent(
    val studyId: Long,
    val studyLeaderId: WriterId,
)
