package kr.co.wground.common.event

import kr.co.wground.global.common.TrackId
import kr.co.wground.study_schedule.domain.enums.Months
import java.time.LocalDateTime

data class StudyRecruitStartedEvent(
    val scheduleId: Long,
    val trackId: TrackId,
    val months: Months,
    val studyRecruitStartedAt: LocalDateTime
)

data class StudyRecruitEndedSoonEvent(
    val scheduleId: Long,
    val trackId: TrackId,
    val months: Months,
    val studyRecruitWillBeEndedAt: LocalDateTime
)

data class StudyEndedSoonEvent(
    val scheduleId: Long,
    val trackId: TrackId,
    val months: Months,
    val studyWillBeEndedAt: LocalDateTime
)
