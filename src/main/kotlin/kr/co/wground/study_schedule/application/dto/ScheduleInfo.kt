package kr.co.wground.study_schedule.application.dto

import java.time.LocalDateTime
import kr.co.wground.study_schedule.domain.enums.Months

data class ScheduleInfo(
    val month: Months,
    val recruitStart: LocalDateTime,
    val studyEnd: LocalDateTime
)