package kr.co.wground.study.application

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.study.application.exception.StudyServiceErrorCode
import kr.co.wground.study.domain.Study
import kr.co.wground.study_schedule.domain.StudySchedule
import kr.co.wground.study.infra.StudyRecruitmentRepository
import kr.co.wground.track.domain.constant.TrackStatus
import org.springframework.stereotype.Component

@Component
class RecruitValidator(
    private val studyRecruitmentRepository: StudyRecruitmentRepository,
) {
    companion object {
        const val MAX_STUDY_CAN_ENROLLED = 2
    }

    fun trackExists(userTrackId: Long, study: Study) {
        if (userTrackId != study.trackId) {
            throw BusinessException(StudyServiceErrorCode.TRACK_MISMATCH)
        }
    }

    fun validateHasMaxStudyLimit(userId: Long, scheduleId: Long) {
        val count = studyRecruitmentRepository.countStudyRecruitment(userId, scheduleId)
        if (count >= MAX_STUDY_CAN_ENROLLED) {
            throw BusinessException(StudyServiceErrorCode.MAX_STUDY_EXCEEDED)
        }
    }

    fun validateSchedule(schedule: StudySchedule) {
        if (schedule.isRecruitmentClosed()) {
            throw BusinessException(StudyServiceErrorCode.STUDY_NOT_PENDING)
        }
    }

    fun validateCurrentMonth(schedule: StudySchedule, current: StudySchedule) {
        if (schedule.months.ordinal < current.months.ordinal) {
            throw BusinessException(StudyServiceErrorCode.STUDY_MONTH_IS_NOT_CURRENT_MONTH)
        }
    }

    fun validateRecruitUserId(recruitUserId: Long, userId: UserId) {
        if (recruitUserId != userId) {
            throw BusinessException(StudyServiceErrorCode.NOT_RECRUITMENT_OWNER)
        }
    }

    fun validateDetermineLeader(study: Study, leaderId: Long) {
        if (!study.isLeader(leaderId)) {
            throw BusinessException(StudyServiceErrorCode.NOT_STUDY_LEADER)
        }
    }

    fun validateGraduated(trackStatus: TrackStatus) {
        if(isGraduated(trackStatus)){
            throw BusinessException(StudyServiceErrorCode.GRADUATED_STUDENT_CANT_RECRUIT_OFFICIAL_STUDY)
        }
    }

    fun isGraduated(trackStatus: TrackStatus): Boolean {
        return trackStatus == TrackStatus.GRADUATED
    }
}