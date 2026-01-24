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
        validateEnrolled(newInfo.studyEnd, track)
        checkOrdinalMonths(newInfo, existSchedules)
    }

    private fun checkOrdinalMonths(
        new: ScheduleInfo,
        existSchedules: List<StudySchedule>
    ) {
        existSchedules.forEach { existSchedule ->
            when {
                existSchedule.months == new.month ->
                    throw BusinessException(StudyServiceErrorCode.DUPLICATE_SCHEDULE_MONTH)

                existSchedule.months.ordinal < new.month.ordinal -> {
                    if (!new.recruitStart.isAfter(existSchedule.studyEndDate)) {
                        throw BusinessException(StudyServiceErrorCode.SCHEDULE_OVERLAP_WITH_PREVIOUS)
                    }
                }

                existSchedule.months.ordinal > new.month.ordinal -> {
                    if (!new.studyEnd.isBefore(existSchedule.recruitStartDate)) {
                        throw BusinessException(StudyServiceErrorCode.SCHEDULE_OVERLAP_WITH_NEXT)
                    }
                }
            }
        }
    }

    private fun validateEnrolled(newStudyEnd: LocalDateTime, track: Track, now: LocalDateTime = LocalDateTime.now()) {
        if (newStudyEnd.isAfter(now) && track.trackStatus != TrackStatus.ENROLLED) {
            throw BusinessException(StudyServiceErrorCode.TRACK_IS_NOT_ENROLLED)
        }
    }
}