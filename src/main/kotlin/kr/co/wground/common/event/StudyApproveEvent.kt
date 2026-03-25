package kr.co.wground.common.event

import kr.co.wground.global.common.UserId

data class StudyApproveEvent(
    val studyId: Long,
    val leaderId: UserId,
    val memberIds: List<UserId>
)