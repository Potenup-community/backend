package kr.co.wground.study.application.dto

import java.time.LocalDateTime

data class ScheduleDto(
    val id: Long,
    val month: String,
    val recruitStartDate: LocalDateTime,
    val recruitEndDate: LocalDateTime,
    val studyEndDate: LocalDateTime,
){
    companion object {
        fun from(dto: StudyQueryDto): ScheduleDto {
            return ScheduleDto(
                id = dto.schedule.id,
                month = dto.schedule.months.month,
                recruitStartDate = dto.schedule.recruitStartDate,
                recruitEndDate = dto.schedule.recruitEndDate,
                studyEndDate = dto.schedule.studyEndDate
            )
        }
    }
}
