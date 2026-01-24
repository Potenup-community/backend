package kr.co.wground.study.presentation.request.schedule

import kr.co.wground.global.common.TrackId
import kr.co.wground.study.application.dto.ScheduleCreateCommand
import kr.co.wground.study.domain.constant.Months
import java.time.LocalDate

data class ScheduleCreateRequest(
    val trackId: TrackId,
    val month: Months,
    val recruitStartDate: LocalDate,
    val recruitEndDate: LocalDate,
    val studyEndDate: LocalDate,
) {
    fun toCommand(): ScheduleCreateCommand {
        return ScheduleCreateCommand(
            trackId = this.trackId,
            month = this.month,
            recruitStartDate = this.recruitStartDate,
            recruitEndDate = this.recruitEndDate,
            studyEndDate = this.studyEndDate,
        )
    }
}