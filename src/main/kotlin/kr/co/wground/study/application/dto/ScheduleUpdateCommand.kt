package kr.co.wground.study.application.dto

import kr.co.wground.global.common.TrackId
import kr.co.wground.study.domain.constant.Months
import java.time.LocalDate

data class ScheduleUpdateCommand(
    val id: Long,
    val trackId: TrackId,
    val months: Months?,
    val recruitStartDate: LocalDate?,
    val recruitEndDate: LocalDate?,
    val studyEndDate: LocalDate?,
)
