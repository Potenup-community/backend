package kr.co.wground.study.application.dto

import kr.co.wground.study.domain.StudySchedule

data class QueryStudyScheduleDto (
    val scheduleId: Long,
    val months: String,
){
    companion object {
        fun from(schedule: StudySchedule): QueryStudyScheduleDto{
            return QueryStudyScheduleDto(
                scheduleId = schedule.id,
                months = schedule.months.month
            )
        }
    }
}