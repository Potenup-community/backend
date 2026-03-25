package kr.co.wground.common.event

import kr.co.wground.global.common.UserId

/**
 * 스터디 결과 보고가 최초 상신되면, 관리자들에게 알림을 보내기 위해 사용됨
 */
data class StudyReportSubmittedEvent(
    val studyId: Long,
    val leaderId: UserId,
)
