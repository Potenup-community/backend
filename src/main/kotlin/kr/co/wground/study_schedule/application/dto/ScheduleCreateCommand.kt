package kr.co.wground.study_schedule.application.dto

import kr.co.wground.global.common.TrackId
import kr.co.wground.study_schedule.domain.StudySchedule
import kr.co.wground.study_schedule.domain.enums.Months
import java.time.LocalDate

data class ScheduleCreateCommand(
    val trackId: TrackId,
    val month: Months,
    val recruitStartDate: LocalDate,
    val recruitEndDate: LocalDate,
    val studyEndDate: LocalDate,
){
    fun toEntity(): StudySchedule {
        return StudySchedule(
            trackId = trackId,
            months = month,
            recruitStartDate = recruitStartDate,
            recruitEndDate = recruitEndDate,
            studyEndDate = studyEndDate,
        )
    }
}
