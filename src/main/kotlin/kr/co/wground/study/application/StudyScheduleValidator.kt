package kr.co.wground.study.application

import java.time.LocalDateTime
import kr.co.wground.exception.BusinessException
import kr.co.wground.study.application.dto.ScheduleInfo
import kr.co.wground.study.application.exception.StudyServiceErrorCode
import kr.co.wground.study.domain.StudySchedule
import kr.co.wground.track.domain.Track
import kr.co.wground.track.domain.constant.TrackStatus
import org.springframework.stereotype.Component

@Component
class StudyScheduleValidator {

    fun validate(
        newInfo: ScheduleInfo,
        track: Track,
        existSchedules: List<StudySchedule>
    ) {
        val now = LocalDateTime.now()
        val isTargetSchedule = newInfo.studyEnd.isAfter(now)

        if (isTargetSchedule && track.trackStatus != TrackStatus.ENROLLED) {
            throw BusinessException(StudyServiceErrorCode.TRACK_IS_NOT_ENROLLED)
        }

        checkOrdinalMonths(newInfo, existSchedules)
    }

    fun checkOrdinalMonths(
        newSchedule: ScheduleInfo,
        existSchedules: List<StudySchedule>
    ) {
        if (existSchedules.isEmpty()) return

        for (schedule in existSchedules) {
            if (schedule.months == newSchedule.month) {
                throw BusinessException(StudyServiceErrorCode.DUPLICATE_SCHEDULE_MONTH)
            }

            if (schedule.months.ordinal < newSchedule.month.ordinal) {
                if (!newSchedule.recruitStart.isAfter(schedule.studyEndDate)) {
                    throw BusinessException(StudyServiceErrorCode.SCHEDULE_OVERLAP_WITH_PREVIOUS)
                }
            }

            if (schedule.months.ordinal > newSchedule.month.ordinal) {
                if (!newSchedule.studyEnd.isBefore(schedule.recruitStartDate)) {
                    throw BusinessException(StudyServiceErrorCode.SCHEDULE_OVERLAP_WITH_NEXT)
                }
            }
        }
    }
}

