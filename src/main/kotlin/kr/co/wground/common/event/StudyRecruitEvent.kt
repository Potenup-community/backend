package kr.co.wground.common.event

import kr.co.wground.global.common.WriterId

/**
 * 스터디에 참여 시, 해당 스터디의 스터디장에게 알림을 보내는데 사용됨
 */
data class StudyRecruitEvent(
    val studyId: Long,
    val leaderId: WriterId,
)
