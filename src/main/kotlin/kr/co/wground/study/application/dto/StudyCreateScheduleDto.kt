package kr.co.wground.study.application.dto

import kr.co.wground.study.domain.StudySchedule

data class StudyCreateScheduleDto (
    val scheduleId: Long,
    val months: String,
){
    companion object {
        fun from(schedule: StudySchedule): StudyCreateScheduleDto{
            return StudyCreateScheduleDto(
                scheduleId = schedule.id,
                months = schedule.months.month
            )
        }
    }
}