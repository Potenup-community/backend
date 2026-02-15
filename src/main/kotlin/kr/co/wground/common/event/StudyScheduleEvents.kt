package kr.co.wground.common.event

import kr.co.wground.global.common.TrackId
import kr.co.wground.study_schedule.domain.enums.Months

data class StudyRecruitStartedEvent(
    val scheduleId: Long,
    val trackId: TrackId,
    val months: Months
)

data class StudyRecruitEndedEvent(
    val scheduleId: Long,
    val trackId: TrackId,
    val months: Months
)

data class StudyEndedEvent(
    val scheduleId: Long,
    val trackId: TrackId,
    val months: Months
)
