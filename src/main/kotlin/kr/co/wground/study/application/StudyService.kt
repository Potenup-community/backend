package kr.co.wground.study.application

import kr.co.wground.exception.BusinessException
import kr.co.wground.study.application.dto.StudyCreateCommand
import kr.co.wground.study.application.dto.StudyUpdateCommand
import kr.co.wground.study.application.exception.StudyServiceErrorCode
import kr.co.wground.study.domain.Study
import kr.co.wground.study.domain.StudyRecruitment
import kr.co.wground.study.domain.Tag
import kr.co.wground.study.domain.constant.RecruitStatus
import kr.co.wground.study.domain.constant.StudyStatus
import kr.co.wground.study.infra.StudyRecruitmentRepository
import kr.co.wground.study.infra.StudyRepository
import kr.co.wground.study.infra.TagRepository
import kr.co.wground.study.presentation.response.study.StudyDetailResponse
import kr.co.wground.track.domain.constant.TrackStatus
import kr.co.wground.track.infra.TrackRepository
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.infra.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StudyService(
    private val studyRepository: StudyRepository,
    private val studyScheduleService: StudyScheduleService,
    private val studyRecruitmentRepository: StudyRecruitmentRepository,
    private val trackRepository: TrackRepository,
    private val tagRepository: TagRepository,
    private val userRepository: UserRepository
) {
    fun createStudy(command: StudyCreateCommand): Long {
        val user = userRepository.findByIdOrNull(command.userId)
            ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

        val track = trackRepository.findByIdOrNull(user.trackId)
            ?: throw BusinessException(StudyServiceErrorCode.TRACK_NOT_FOUND)

        if (track.trackStatus != TrackStatus.ENROLLED) {
            throw BusinessException(StudyServiceErrorCode.TRACK_IS_NOT_ENROLLED)
        }

        val schedule = studyScheduleService.getCurrentSchedule(track.trackId)
            ?: throw BusinessException(StudyServiceErrorCode.NO_CURRENT_SCHEDULE)

        val enrolledStudyCount = studyRecruitmentRepository.countActiveEnrolledStudy(user.userId, schedule.id)
        if (enrolledStudyCount >= 2) {
            throw BusinessException(StudyServiceErrorCode.MAX_STUDY_EXCEEDED)
        }

        val tags = resolveTags(command.tags)

        val scheduleEntity = studyScheduleService.getScheduleEntity(schedule.id)

        val study = Study(
            name = command.name,
            leaderId = user.userId,
            trackId = track.trackId,
            schedule = scheduleEntity,
            description = command.description,
            status = StudyStatus.PENDING,
            capacity = command.capacity,
            budget = command.budget,
            externalChatUrl = command.chatUrl,
            referenceUrl = command.refUrl
        )
        tags.forEach { study.addTag(it) }
        val savedStudy = studyRepository.save(study)

        val leaderRecruitment = StudyRecruitment.createByLeader(user.userId, savedStudy)
        studyRecruitmentRepository.save(leaderRecruitment)

        return savedStudy.id
    }

    fun updateStudy(command: StudyUpdateCommand) {
        val study = getStudyEntity(command.studyId)

        if (study.leaderId != command.userId) {
            throw BusinessException(StudyServiceErrorCode.NOT_STUDY_LEADER)
        }

        val newTags: List<Tag>? = command.tags?.let { tagNames ->
            resolveTags(tagNames)
        }

        study.updateStudyInfo(
            newName = command.name ?: study.name,
            newDescription = command.description ?: study.description,
            newCapacity = command.capacity ?: study.capacity,
            newBudget = command.budget ?: study.budget,
            newChatUrl = command.chatUrl ?: study.externalChatUrl,
            newRefUrl = command.refUrl ?: study.referenceUrl,
            newTags = newTags
        )
    }

    fun deleteStudy(studyId: Long, userId: Long, isAdmin: Boolean) {
        val study = getStudyEntity(studyId)

        if (isAdmin) {
            studyRepository.delete(study)
        } else {
            if (study.leaderId != userId) {
                throw BusinessException(StudyServiceErrorCode.NOT_STUDY_LEADER)
            }
            study.validateHardDeletable()
            studyRepository.delete(study)
        }
    }

    fun approveStudy(studyId: Long) {
        val study = getStudyEntity(studyId)

        study.approve()

        val pendingApplications =
            studyRecruitmentRepository.findAllByStudyIdAndRecruitStatus(studyId, RecruitStatus.PENDING)
        pendingApplications.forEach { it.updateRecruitStatus(RecruitStatus.REJECTED) }
    }

    fun rejectStudy(studyId: Long) {
        val study =
            studyRepository.findByIdOrNull(studyId) ?: throw BusinessException(StudyServiceErrorCode.STUDY_NOT_FOUND)

        study.reject()

        study.recruitments.forEach {
            it.updateRecruitStatus(RecruitStatus.REJECTED)
        }
    }

    @Transactional(readOnly = true)
    fun getStudy(studyId: Long, userId: Long?): StudyDetailResponse {
        val study = getStudyEntity(studyId)

        // 채팅 링크 마스킹 로직
        val canViewChatUrl = if (userId == null) false else {
            study.leaderId == userId || // 스터디장이거나
                    studyRecruitmentRepository.existsByStudyIdAndUserIdAndRecruitStatus(
                        studyId,
                        userId,
                        RecruitStatus.APPROVED
                    ) // 참여자거나
        }

        return StudyDetailResponse.from(study, canViewChatUrl)
    }

    private fun resolveTags(tagNames: List<String>): List<Tag> {
        return tagNames.map { name ->
            tagRepository.findByName(name) ?: tagRepository.save(Tag.create(name))
        }
    }

    private fun getStudyEntity(id: Long): Study {
        return studyRepository.findByIdOrNull(id)
            ?: throw BusinessException(StudyServiceErrorCode.STUDY_NOT_FOUND)
    }
}