package kr.co.wground.common.event

import kr.co.wground.global.common.UserId

/**
 * 스터디장에 의해 스터디 참여가 승인되면, 참여 신청자에게 알림을 보내기 위해 사용됨
 */
@Deprecated(message = "스터디 참여에 대한 스터디장의 승인 절차가 없어지고, 무조건 선착순 참여로 변경됨에 따라 해당 이벤트는 더 이상 필요하지 않습니다.")
data class StudyRecruitmentEvent(
    val studyId: Long,
    val userId: UserId
)
