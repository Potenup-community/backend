package kr.co.wground.common.event

import kr.co.wground.study.domain.constant.RecruitStatus

data class StudyDetermineEvent(
    val studyId: Long,
    val recruitmentId: Long,
    val recruitStatus: RecruitStatus,
)
