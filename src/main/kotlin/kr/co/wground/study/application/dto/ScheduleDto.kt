package kr.co.wground.study.application.dto

import java.time.LocalDateTime
import kr.co.wground.study.domain.constant.Months

data class ScheduleDto(
    val id: Long,
    val month: Months,
    val recruitStartDate: LocalDateTime,
    val recruitEndDate: LocalDateTime,
    val studyEndDate: LocalDateTime,
){
    companion object {
        fun from(dto: StudyQueryDto): ScheduleDto {
            return ScheduleDto(
                id = dto.schedule.id,
                month = dto.schedule.months,
                recruitStartDate = dto.schedule.recruitStartDate,
                recruitEndDate = dto.schedule.recruitEndDate,
                studyEndDate = dto.schedule.studyEndDate
            )
        }
    }
}
