package kr.co.wground.common.event

import kr.co.wground.study.domain.constant.RecruitStatus

data class StudyDecidedEvent(
    val studyId: Long,
    val recruitmentId: Long, //recruitmentId -> UserId아님
    val recruitStatus: RecruitStatus, // ACCEPTED or REJECTED
)
