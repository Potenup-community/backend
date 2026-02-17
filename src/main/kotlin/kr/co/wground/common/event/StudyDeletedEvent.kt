package kr.co.wground.common.event

import kr.co.wground.global.common.UserId

/**
 * 스터디가 삭제되면, 해당 스터디의 모든 참가자에게 알림을 보내는데 사용됨
 */
data class StudyDeletedEvent(
    val studyId: Long,
    val studyTitle: String,
    val userIds: List<UserId>,
)
