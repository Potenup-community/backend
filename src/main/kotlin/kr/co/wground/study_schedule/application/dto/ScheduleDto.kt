package kr.co.wground.study_schedule.application.dto

import kr.co.wground.study_schedule.domain.StudySchedule
import java.time.LocalDateTime

data class ScheduleDto(
    val id: Long,
    val month: String,
    val recruitStartDate: LocalDateTime,
    val recruitEndDate: LocalDateTime,
    val studyEndDate: LocalDateTime,
){
    companion object {
        fun from(schedule: StudySchedule): ScheduleDto {
            return ScheduleDto(
                id = schedule.id,
                month = schedule.months.month,
                recruitStartDate = schedule.recruitStartDate,
                recruitEndDate = schedule.recruitEndDate,
                studyEndDate = schedule.studyEndDate
            )
        }
    }
}
