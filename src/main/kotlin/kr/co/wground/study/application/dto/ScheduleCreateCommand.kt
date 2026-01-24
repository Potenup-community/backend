package kr.co.wground.study.application.dto

import kr.co.wground.global.common.TrackId
import kr.co.wground.study.domain.StudySchedule
import kr.co.wground.study.domain.constant.Months
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
