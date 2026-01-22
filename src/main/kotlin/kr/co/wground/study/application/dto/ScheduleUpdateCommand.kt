package kr.co.wground.study.application.dto

import kr.co.wground.study.domain.constant.Months
import java.time.LocalDate
import java.time.LocalDateTime

data class ScheduleUpdateCommand(
    val id: Long,
    val months: Months,
    val recruitStartDate: LocalDate,
    val recruitEndDate: LocalDateTime,
    val studyEndDate: LocalDateTime,
)
