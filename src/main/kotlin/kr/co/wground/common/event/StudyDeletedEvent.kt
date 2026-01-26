package kr.co.wground.common.event

import kr.co.wground.global.common.UserId

data class StudyDeletedEvent(
    val studyId: Long,
    val studyTitle: String,
    val recruitUserIds: List<UserId>,
)
