package kr.co.wground.study.presentation.request.schedule

import kr.co.wground.study.application.dto.ScheduleUpdateCommand
import kr.co.wground.study.domain.constant.Months
import java.time.LocalDate
import java.time.LocalDateTime

data class ScheduleUpdateRequest(
    val id: Long,
    val months: Months,
    val recruitStartDate: LocalDate,
    val recruitEndDate: LocalDateTime,
    val studyEndDate: LocalDateTime,
) {
    fun toCommand(): ScheduleUpdateCommand {
        return ScheduleUpdateCommand(
            id = id,
            months = months,
            recruitStartDate = recruitStartDate,
            recruitEndDate = recruitEndDate,
            studyEndDate = studyEndDate,
        )
    }
}
