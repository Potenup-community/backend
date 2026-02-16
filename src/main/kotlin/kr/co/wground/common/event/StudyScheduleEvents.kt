package kr.co.wground.common.event

import kr.co.wground.global.common.TrackId
import kr.co.wground.study_schedule.domain.enums.Months

data class StudyRecruitStartedEvent(
    val scheduleId: Long,
    val trackId: TrackId,
    val months: Months
)

data class StudyRecruitEndedSoonEvent(
    val scheduleId: Long,
    val trackId: TrackId,
    val months: Months
)

data class StudyEndedSoonEvent(
    val scheduleId: Long,
    val trackId: TrackId,
    val months: Months
)
