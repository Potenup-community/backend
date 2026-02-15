package kr.co.wground.study_schedule.presentation.request

import kr.co.wground.global.common.TrackId
import kr.co.wground.study_schedule.application.dto.ScheduleUpdateCommand
import kr.co.wground.study_schedule.domain.enums.Months
import java.time.LocalDate

data class ScheduleUpdateRequest(
    val trackId: TrackId,
    val months: Months?,
    val recruitStartDate: LocalDate?,
    val recruitEndDate: LocalDate?,
    val studyEndDate: LocalDate?,
) {
    fun toCommand(id: Long): ScheduleUpdateCommand {
        return ScheduleUpdateCommand(
            id = id,
            trackId = trackId,
            months = months,
            recruitStartDate = recruitStartDate,
            recruitEndDate = recruitEndDate,
            studyEndDate = studyEndDate,
        )
    }
}
