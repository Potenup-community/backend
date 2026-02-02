package kr.co.wground.common.event

import kr.co.wground.global.common.UserId
import kr.co.wground.study.domain.constant.RecruitStatus

data class StudyDetermineEvent(
    val studyId: Long,
    val userId: UserId,
    val recruitStatus: RecruitStatus,
)
