package kr.co.wground.study.presentation.response.schedule

import kr.co.wground.study.domain.constant.Months
import java.time.LocalDateTime

data class ScheduleQueryResponse(
    val id: Long,
    val trackId: Long,
    val months: Months,
    val recruitStartDate: LocalDateTime,
    val recruitEndDate: LocalDateTime,
    val studyEndDate: LocalDateTime,
)
