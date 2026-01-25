package kr.co.wground.common.event

import kr.co.wground.global.common.UserId
import kr.co.wground.global.common.WriterId

data class StudyAppliedEvent(
    val studyId: Long,
    val studyWriterId: WriterId,
    val applicantId: UserId,
)
