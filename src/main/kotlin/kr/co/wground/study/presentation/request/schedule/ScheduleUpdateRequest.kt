package kr.co.wground.study.presentation.request.schedule

import kr.co.wground.global.common.TrackId
import kr.co.wground.study.application.dto.ScheduleUpdateCommand
import kr.co.wground.study.domain.constant.Months
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
