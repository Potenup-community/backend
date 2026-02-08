package kr.co.wground.study_schedule.presentation.response

import kr.co.wground.study_schedule.domain.enums.Months
import java.time.LocalDateTime

data class ScheduleQueryResponse(
    val id: Long,
    val trackId: Long,
    val months: Months,
    val monthName: String,
    val recruitStartDate: LocalDateTime,
    val recruitEndDate: LocalDateTime,
    val studyEndDate: LocalDateTime,
)
