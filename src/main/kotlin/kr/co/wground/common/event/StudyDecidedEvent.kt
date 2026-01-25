package kr.co.wground.common.event

import kr.co.wground.global.common.UserId

data class StudyDecidedEvent(
    val studyId: Long,
    val applicantId: UserId,
    val status: Status, // ACCEPTED or REJECTED
)
