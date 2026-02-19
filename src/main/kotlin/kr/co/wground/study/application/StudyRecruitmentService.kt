package kr.co.wground.study.application

import kr.co.wground.common.event.StudyRecruitEvent
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.TrackId
import kr.co.wground.global.common.UserId
import kr.co.wground.shop.application.dto.EquippedItem
import kr.co.wground.shop.application.dto.EquippedItem.Companion.from
import kr.co.wground.shop.application.query.InventoryQueryPort
import kr.co.wground.study.application.exception.StudyServiceErrorCode
import kr.co.wground.study.domain.Study
import kr.co.wground.study.domain.StudyRecruitment
import kr.co.wground.study_schedule.domain.StudySchedule
import kr.co.wground.study.infra.StudyRecruitmentRepository
import kr.co.wground.study.infra.StudyRepository
import kr.co.wground.study_schedule.infra.StudyScheduleRepository
import kr.co.wground.study.presentation.response.recruit.StudyRecruitmentResponse
import kr.co.wground.study_schedule.application.exception.StudyScheduleServiceErrorCode
import kr.co.wground.track.application.exception.TrackServiceErrorCode
import kr.co.wground.track.domain.Track
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
    private val eventPublisher: ApplicationEventPublisher,
    private val inventoryQueryPort: InventoryQueryPort
) {

    fun participate(userId: Long, studyId: Long): Long {

        val user = findUserOrThrows(userId)
        val track = findTrackByIdOrThrows(user.trackId)
        recruitValidator.validateGraduated(track.trackStatus)

        val study = findStudyByIdOrThrows(studyId)

        recruitValidator.trackExists(user.trackId, study)

        val schedule = findScheduleByIdOrThrows(study.scheduleId)
        val requestMonth = scheduleRepository.findAllByTrackIdOrderByMonthsAsc(user.trackId)
            .firstOrNull { it.isCurrentRound() }
            ?: throw BusinessException(StudyScheduleServiceErrorCode.SCHEDULE_NOT_FOUND)

        recruitValidator.validateSchedule(schedule)
        recruitValidator.validateCurrentMonth(schedule, requestMonth)
        recruitValidator.validateHasMaxStudyLimit(userId, schedule.id)

        study.participate(userId)
        val saved = studyRepository.save(study)

        eventPublisher.publishEvent(
            StudyRecruitEvent(
                studyId = saved.id,
                leaderId = saved.leaderId
            )
        )

        return saved.recruitments.find { it.userId == userId }?.id
            ?: throw RuntimeException("알 수 없는 이유로 스터디 생성에 실패했습니다.")
    }

    fun cancelRecruit(userId: Long, recruitmentId: Long) {

        val recruitment = findStudyRecruitmentByIdOrThrows(recruitmentId)

        findScheduleByIdOrThrows(recruitment.study.scheduleId)

        recruitValidator.validateRecruitUserId(recruitment.userId, userId)

        recruitment.study.withdraw(userId)

        studyRecruitmentRepository.delete(recruitment)
    }

    @Transactional(readOnly = true)
    fun getMyRecruitments(userId: Long): List<StudyRecruitmentResponse> {

        val recruitments = findAllStudyRecruitmentsByUserId(userId)
        if (recruitments.isEmpty()) {
            throw BusinessException(StudyServiceErrorCode.RECRUITMENT_NOT_FOUND)
        }
        val equippedItems = inventoryQueryPort.getEquipItems(listOf(userId)).groupBy { it.userId }
            .mapValues { (_, rows) -> rows.map(EquippedItem::from) }
        val user = findUserOrThrows(userId)
        val track = findTrackByIdOrThrows(user.trackId)
        return recruitments.map { recruitment ->
            StudyRecruitmentResponse.of(recruitment, user.name, track.trackName, equippedItems[userId] ?: emptyList())
        }
    }

    @Transactional(readOnly = true)
    fun getStudyRecruitments(userId: UserId, studyId: Long): List<StudyRecruitmentResponse> {
        val study = findStudyByIdOrThrows(studyId)

        // To Do: 신청이 아니라 참여니까 스터디장이 아닌 참가자들도 볼 수 있어야 하지 않을까?
        recruitValidator.validateDetermineLeader(study, userId)

        val recruitments = studyRecruitmentRepository.findAllByStudyId(studyId)

        val userIds = recruitments.map { it.userId }.toSet()
        val users = userRepository.findAllById(userIds).associateBy { it.userId }
        val track = findTrackByIdOrThrows(study.trackId)
        val equippedItems = inventoryQueryPort.getEquipItems(userIds.toList()).groupBy { it.userId }
            .mapValues { (_, rows) -> rows.map(EquippedItem::from) }

        return recruitments.map { recruitment ->
            val applicant = users[recruitment.userId]
                ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

            StudyRecruitmentResponse.of(
                recruitment,
                applicant.name,
                track.trackName,
                equippedItems[recruitment.userId] ?: emptyList()
            )
        }
    }

    // ----- helpers

    private fun findUserOrThrows(userId: UserId): User {
        return userRepository.findByIdOrNull(userId)
            ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)
    }

    private fun findAllStudyRecruitmentsByUserId(userId: Long): List<StudyRecruitment> {
        return studyRecruitmentRepository.findAllByUserId(userId)
    }

    private fun findStudyRecruitmentByIdOrThrows(id: Long): StudyRecruitment {
        return studyRecruitmentRepository.findByIdOrNull(id)
            ?: throw BusinessException(StudyServiceErrorCode.RECRUITMENT_NOT_FOUND)
    }

    private fun findStudyByIdOrThrows(studyId: Long): Study {
        return studyRepository.findByIdOrNull(studyId)
            ?: throw BusinessException(StudyServiceErrorCode.STUDY_NOT_FOUND)
    }

    private fun findTrackByIdOrThrows(trackId: TrackId): Track {
        return trackRepository.findByIdOrNull(trackId)
            ?: throw BusinessException(TrackServiceErrorCode.TRACK_NOT_FOUND)
    }

    private fun findScheduleByIdOrThrows(scheduleId: Long): StudySchedule {
        return scheduleRepository.findByIdOrNull(scheduleId)
            ?: throw BusinessException(StudyScheduleServiceErrorCode.SCHEDULE_NOT_FOUND)
    }
}