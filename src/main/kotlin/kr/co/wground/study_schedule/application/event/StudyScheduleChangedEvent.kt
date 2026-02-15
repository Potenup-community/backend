package kr.co.wground.study_schedule.application.event

import kr.co.wground.global.common.TrackId
import kr.co.wground.study_schedule.domain.enums.Months
import java.time.LocalDateTime
import kr.co.wground.study_schedule.domain.StudySchedule

data class StudyScheduleChangedEvent(
    val scheduleId: Long,
    val trackId: TrackId,
    val months: Months,
    val recruitStartDate: LocalDateTime,
    val recruitEndDate: LocalDateTime,
    val studyEndDate: LocalDateTime,
    val type: EventType
) {
    enum class EventType {
        CREATED,
        UPDATED,
        DELETED
    }

    companion object {
        fun of(schedule: StudySchedule, studyScheduleEventType: EventType): StudyScheduleChangedEvent {
            return StudyScheduleChangedEvent(
                scheduleId = schedule.id,
                trackId = schedule.trackId,
                months = schedule.months,
                recruitStartDate = schedule.recruitStartDate,
                recruitEndDate = schedule.recruitEndDate,
                studyEndDate = schedule.studyEndDate,
                type = studyScheduleEventType
            )
        }
    }
}

