package kr.co.wground.study.application

import java.time.LocalDateTime
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.TrackId
import kr.co.wground.global.common.UserId
import kr.co.wground.study.application.exception.StudyServiceErrorCode
import kr.co.wground.study.domain.Study
import kr.co.wground.study.domain.StudyRecruitment
import kr.co.wground.study.domain.StudySchedule
import kr.co.wground.study.domain.constant.RecruitStatus
import kr.co.wground.study.domain.constant.StudyStatus
import kr.co.wground.study.infra.StudyRecruitmentRepository
import kr.co.wground.study.infra.StudyRepository
import kr.co.wground.study.infra.StudyScheduleRepository
import kr.co.wground.study.presentation.response.recruit.StudyRecruitmentResponse
import kr.co.wground.track.application.exception.TrackServiceErrorCode
import kr.co.wground.track.infra.TrackRepository
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.domain.User
import kr.co.wground.user.infra.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class StudyRecruitmentService(
    private val studyRecruitmentRepository: StudyRecruitmentRepository,
    private val studyRepository: StudyRepository,
    private val userRepository: UserRepository,
    private val trackRepository: TrackRepository,
    private val scheduleRepository: StudyScheduleRepository
) {

    fun requestRecruit(userId: Long, studyId: Long, appeal: String): Long {
        val user = findUser(userId)

        val study = findStudyById(studyId)
        val schedule = study.schedule
        val requestMonth = scheduleRepository.findAllByTrackIdOrderByMonthsAsc(user.trackId)
            .firstOrNull { it.isCurrentRound() } ?: throw BusinessException(StudyServiceErrorCode.SCHEDULE_NOT_FOUND)

        validateSchedule(schedule)
        validateCurrentMonth(schedule,requestMonth)
        validateApply(user.trackId, study)
        validateDuplicateRecruit(userId, studyId)
        validateHasMaxStudyLimit(userId, study.schedule.id)

        val recruitment = StudyRecruitment.apply(userId, appeal, study)
        val savedRecruitment = studyRecruitmentRepository.save(recruitment)

        return savedRecruitment.id
    }

    fun cancelRecruit(userId: Long, recruitmentId: Long) {
        val recruitment = getRecruitment(recruitmentId)

        if (recruitment.userId != userId) {
            throw BusinessException(StudyServiceErrorCode.NOT_RECRUITMENT_OWNER)
        }

        if (recruitment.study.isLeader(userId)) {
            throw BusinessException(StudyServiceErrorCode.LEADER_CANNOT_LEAVE)
        }

        if (recruitment.recruitStatus == RecruitStatus.APPROVED || recruitment.recruitStatus == RecruitStatus.PENDING) {
            recruitment.study.decreaseMemberCount()
        }
        recruitment.updateRecruitStatus(RecruitStatus.CANCELLED)
    }

    fun determineRecruit(leaderId: Long, recruitmentId: Long, newStatus: RecruitStatus) {
        val recruitment = getRecruitment(recruitmentId)

        if (!recruitment.study.isLeader(leaderId)) {
            throw BusinessException(StudyServiceErrorCode.NOT_STUDY_LEADER)
        }

        if (newStatus == RecruitStatus.APPROVED) {
            recruitment.study.increaseMemberCount()
        }

        recruitment.updateRecruitStatus(newStatus)
    }

    @Transactional(readOnly = true)
    fun getMyRecruitments(userId: Long): List<StudyRecruitmentResponse> {
        val recruitments = findStudyByUserId(userId)
        val user = findUser(userId)
        val trackName = findTrackName(user.trackId)
        return recruitments.map { recruitment ->
            StudyRecruitmentResponse.of(recruitment, user.name, trackName)
        }
    }

    @Transactional(readOnly = true)
    fun getStudyRecruitments(userId: UserId, studyId: Long): List<StudyRecruitmentResponse> {
        val study = findStudyById(studyId)

        if (!study.isLeader(userId)) {
            throw BusinessException(StudyServiceErrorCode.NOT_STUDY_LEADER)
        }

        val recruitments = studyRecruitmentRepository.findAllByStudyId(studyId)

        val userIds = recruitments.map { it.userId }.toSet()
        val users = userRepository.findAllById(userIds).associateBy { it.userId }
        val trackName = findTrackName(study.trackId)
        return recruitments.map { recruitment ->
            val applicant = users[recruitment.userId]
                ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

            StudyRecruitmentResponse.of(recruitment, applicant.name, trackName)
        }
    }

    private fun validateApply(userTrackId: Long, study: Study) {
        if (userTrackId != study.trackId) {
            throw BusinessException(StudyServiceErrorCode.TRACK_MISMATCH)
        }
        if (study.status != StudyStatus.PENDING) {
            throw BusinessException(StudyServiceErrorCode.STUDY_NOT_RECRUITING)
        }
    }

    private fun validateDuplicateRecruit(userId: Long, studyId: Long) {
        val statusList = listOf(RecruitStatus.PENDING, RecruitStatus.APPROVED, RecruitStatus.REJECTED)
        if (studyRecruitmentRepository.existsByStudyIdAndUserIdAndRecruitStatusIn(
                studyId, userId, statusList
            )
        ) {
            throw BusinessException(StudyServiceErrorCode.ALREADY_APPLIED)
        }
    }

    private fun validateHasMaxStudyLimit(userId: Long, scheduleId: Long) {
        val count = studyRecruitmentRepository.countActiveEnrolledStudy(userId, scheduleId)
        if (count >= 2) {
            throw BusinessException(StudyServiceErrorCode.MAX_STUDY_EXCEEDED)
        }
    }

    private fun validateSchedule(schedule: StudySchedule) {
        if (schedule.isRecruitmentClosed()) {
            throw BusinessException(StudyServiceErrorCode.STUDY_NOT_RECRUITING)
        }
    }

    private fun validateCurrentMonth(schedule: StudySchedule, current: StudySchedule) {
        if(schedule.months.ordinal < current.months.ordinal){
            throw BusinessException(StudyServiceErrorCode.STUDY_MONTH_IS_NOT_CURRENT_MONTH)
        }
    }

    private fun findUser(userId: UserId): User {
        return userRepository.findByIdOrNull(userId)
            ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)
    }

    private fun findStudyByUserId(userId: Long): List<StudyRecruitment> {
        return studyRecruitmentRepository.findAllByUserId(userId)
    }

    private fun getRecruitment(id: Long): StudyRecruitment {
        return studyRecruitmentRepository.findByIdOrNull(id)
            ?: throw BusinessException(StudyServiceErrorCode.RECRUITMENT_NOT_FOUND)
    }

    private fun findStudyById(studyId: Long): Study {
        return studyRepository.findByIdOrNull(studyId)
            ?: throw BusinessException(StudyServiceErrorCode.STUDY_NOT_FOUND)
    }

    private fun findTrackName(trackId: TrackId): String {
        val track = trackRepository.findByIdOrNull(trackId)
            ?: throw BusinessException(TrackServiceErrorCode.TRACK_NOT_FOUND)
        return track.trackName
    }
}