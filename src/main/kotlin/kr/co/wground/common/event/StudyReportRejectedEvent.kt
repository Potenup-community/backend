package kr.co.wground.common.event

import kr.co.wground.global.common.UserId

/**
 * 스터디 결과 보고가 반려되면, 스터디장에게 알림을 보내기 위해 사용됨
 */
data class StudyReportRejectedEvent(
    val studyId: Long,
    val leaderId: UserId,
    val adminId: UserId,
)
