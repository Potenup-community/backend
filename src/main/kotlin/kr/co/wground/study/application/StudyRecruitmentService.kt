package kr.co.wground.study.application

import kr.co.wground.common.event.StudyRecruitEvent
import kr.co.wground.common.event.StudyDetermineEvent
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.TrackId
import kr.co.wground.global.common.UserId
import kr.co.wground.study.application.exception.StudyServiceErrorCode
import kr.co.wground.study.domain.Study
import kr.co.wground.study.domain.StudyRecruitment
import kr.co.wground.study.domain.StudySchedule
import kr.co.wground.study.domain.constant.RecruitStatus
import kr.co.wground.study.infra.StudyRecruitmentRepository
import kr.co.wground.study.infra.StudyRepository
import kr.co.wground.study.infra.StudyScheduleRepository
import kr.co.wground.study.presentation.response.recruit.StudyRecruitmentResponse
import kr.co.wground.track.application.exception.TrackServiceErrorCode
import kr.co.wground.track.infra.TrackRepository
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.domain.User
import kr.co.wground.user.infra.UserRepository
import org.springframework.context.ApplicationEventPublisher
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
    private val scheduleRepository: StudyScheduleRepository,
    private val recruitValidator: RecruitValidator,
    private val eventPublisher: ApplicationEventPublisher
) {

    fun requestRecruit(userId: Long, studyId: Long, appeal: String): Long {
        val user = findUser(userId)

        val study = findStudyById(studyId)
        val schedule = findScheduleById(study.scheduleId)

        val requestMonth = scheduleRepository.findAllByTrackIdOrderByMonthsAsc(user.trackId)
            .firstOrNull { it.isCurrentRound() } ?: throw BusinessException(StudyServiceErrorCode.SCHEDULE_NOT_FOUND)

        recruitValidator.validateSchedule(schedule)
        recruitValidator.validateCurrentMonth(schedule, requestMonth)
        recruitValidator.validateApply(user.trackId, study)
        recruitValidator.validateDuplicateRecruit(userId, studyId)
        recruitValidator.validateHasMaxStudyLimit(userId, schedule.id)

        val recruitment = StudyRecruitment.apply(userId, appeal, study)
        val savedRecruitment = studyRecruitmentRepository.save(recruitment)

        eventPublisher.publishEvent(
            StudyRecruitEvent(
                studyId = study.id,
                studyLeaderId = study.leaderId
            )
        )
        return savedRecruitment.id
    }

    fun cancelRecruit(userId: Long, recruitmentId: Long) {
        val recruitment = findRecruitment(recruitmentId)
        val schedule = findScheduleById(recruitment.study.scheduleId)

        recruitValidator.validateRecruitUserId(recruitment.userId, userId)
        recruitValidator.validateLeaderCancel(recruitment.study, userId)

        if (recruitment.recruitStatus == RecruitStatus.APPROVED || recruitment.recruitStatus == RecruitStatus.PENDING) {
            recruitment.study.decreaseMemberCount(schedule.isRecruitmentClosed())
        }
        recruitment.cancel()
    }

    fun determineRecruit(leaderId: Long, recruitmentId: Long, newStatus: RecruitStatus) {
        val recruitment = findRecruitment(recruitmentId)
        val schedule = findScheduleById(recruitment.study.scheduleId)

        recruitValidator.validateDetermineLeader(recruitment.study, leaderId)

        if (newStatus == RecruitStatus.APPROVED) {
            recruitment.study.increaseMemberCount(schedule.recruitEndDate, schedule.isRecruitmentClosed())
        }

        recruitment.updateRecruitStatus(newStatus)

        eventPublisher.publishEvent(
            StudyDetermineEvent(
                studyId = recruitment.study.id,
                recruitmentId = recruitment.userId,
                recruitStatus = recruitment.recruitStatus
            )
        )
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

        recruitValidator.validateDetermineLeader(study, userId)

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

    private fun findUser(userId: UserId): User {
        return userRepository.findByIdOrNull(userId)
            ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)
    }

    private fun findStudyByUserId(userId: Long): List<StudyRecruitment> {
        return studyRecruitmentRepository.findAllByUserId(userId)
    }

    private fun findRecruitment(id: Long): StudyRecruitment {
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

    private fun findScheduleById(scheduleId: Long): StudySchedule {
        return scheduleRepository.findByIdOrNull(scheduleId)
            ?: throw BusinessException(StudyServiceErrorCode.SCHEDULE_NOT_FOUND)
    }
}