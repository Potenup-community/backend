package kr.co.wground.study_schedule.application.dto

import kr.co.wground.global.common.TrackId
import kr.co.wground.study_schedule.domain.enums.Months
import java.time.LocalDate

data class ScheduleUpdateCommand(
    val id: Long,
    val trackId: TrackId,
    val months: Months?,
    val recruitStartDate: LocalDate?,
    val recruitEndDate: LocalDate?,
    val studyEndDate: LocalDate?,
)
