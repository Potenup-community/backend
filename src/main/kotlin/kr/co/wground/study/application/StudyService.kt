package kr.co.wground.study.application

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.study.application.dto.LeaderDto
import kr.co.wground.study.application.dto.ScheduleDto
import kr.co.wground.study.application.dto.StudyCreateCommand
import kr.co.wground.study.application.dto.StudySearchCondition
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
import kr.co.wground.study.presentation.response.study.StudyQueryResponse
import kr.co.wground.track.domain.constant.TrackStatus
import kr.co.wground.track.infra.TrackRepository
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.infra.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class StudyService(
    private val studyRepository: StudyRepository,
    private val studyScheduleService: StudyScheduleService,
    private val studyRecruitmentRepository: StudyRecruitmentRepository,
    private val trackRepository: TrackRepository,
    private val tagRepository: TagRepository,
    private val userRepository: UserRepository
) {
    companion object {
        const val MAX_ENROLLED_STUDY = 2
        private val log = LoggerFactory.getLogger(StudyService::class.java)
    }

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
        if (enrolledStudyCount >= MAX_ENROLLED_STUDY) {
            throw BusinessException(StudyServiceErrorCode.MAX_STUDY_EXCEEDED)
        }

        val tags = resolveTags(command.tags)

        val scheduleEntity = studyScheduleService.getScheduleEntity(schedule.id)

        val study = Study(
            name = command.name,
            leaderId = user.userId,
            trackId = track.trackId,
            scheduleId = scheduleEntity.id,
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

    fun updateStudy(command: StudyUpdateCommand): Long {
        val study = getStudyEntity(command.studyId)
        val schedule = studyScheduleService.getScheduleEntity(command.scheduleId)

        if (!study.isLeader(command.userId)) {
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
            newScheduleId = command.scheduleId,
            newChatUrl = command.chatUrl ?: study.externalChatUrl,
            newRefUrl = command.refUrl ?: study.referenceUrl,
            newTags = newTags,
            isRecruitmentClosed = schedule.isRecruitmentClosed(),
        )
        return study.id
    }

    fun deleteStudy(studyId: Long, userId: Long, isAdmin: Boolean) {
        val study = getStudyEntity(studyId)

        if (isAdmin) {
            study.isLeader(userId)
            study.validateHardDeletable()
        }

        studyRepository.delete(study)
    }

    fun approveStudy(studyId: Long) {
        val study = getStudyEntity(studyId)

        study.approve()

        studyRecruitmentRepository.rejectAllByStudyIdWithExceptStatus(studyId, RecruitStatus.APPROVED)

    }

    fun rejectStudy(studyId: Long) {
        val study = getStudyEntity(studyId)

        study.reject()

        studyRecruitmentRepository.rejectAllByStudyIdWithExceptStatus(studyId, RecruitStatus.CANCELLED)
    }

    @Transactional(readOnly = true)
    fun searchStudies(
        condition: StudySearchCondition,
        pageable: Pageable,
        userId: UserId
    ): Slice<StudyQueryResponse> {
        val result = studyRepository.searchStudies(condition, pageable)

        val joinedStudyIds = if (userId != null) {
            val studyIds = result.content.map { it.study.id }
            studyRecruitmentRepository.findApprovedStudyIdsByUserIdAndStudyIds(userId, studyIds).toSet()
        } else emptySet()

        return result.map { dto ->
            val leaderDto = LeaderDto.from(dto)
            val scheduleDto = ScheduleDto.from(dto)

            val isJoined = joinedStudyIds.contains(dto.study.id)
            val canViewChatUrl = (userId == dto.study.leaderId) || isJoined
            val isRecruitmentClosed = dto.schedule.isRecruitmentClosed()

            StudyQueryResponse.of(
                study = dto.study,
                canViewChatUrl = canViewChatUrl,
                schedule = scheduleDto,
                userId = userId,
                leaderDto = leaderDto,
                isRecruitmentClosed = isRecruitmentClosed
            )
        }
    }

    @Transactional(readOnly = true)
    fun getStudy(studyId: Long, userId: Long?): StudyDetailResponse {
        val study = getStudyEntity(studyId)
        val schedule = studyScheduleService.getScheduleEntity(study.scheduleId)

        // 채팅 링크 마스킹 로직
        val canViewChatUrl = if (userId == null) false else {
            study.leaderId == userId || // 스터디장이거나
                    studyRecruitmentRepository.existsByStudyIdAndUserIdAndRecruitStatusIn(
                        studyId,
                        userId,
                        listOf(RecruitStatus.APPROVED)
                    ) // 참여자거나
        }

        return StudyDetailResponse.of(study, canViewChatUrl, schedule, userId)
    }

    private fun getStudyEntity(id: Long): Study {
        return studyRepository.findByIdOrNull(id)
            ?: throw BusinessException(StudyServiceErrorCode.STUDY_NOT_FOUND)
    }

    private fun resolveTags(tagNames: List<String>): List<Tag> {
        if (tagNames.isEmpty()) return emptyList()

        val distinctNames = tagNames.map { Tag.normalize(it) }.distinct()
        val existTags = tagRepository.findByNameIn(distinctNames)

        val existingTagNames = existTags.map { it.name }.toSet()
        val newTagNames = distinctNames.filter { !existingTagNames.contains(it) }

        val newTags = newTagNames.map { name ->
            createTag(name)
        }

        return existTags + newTags
    }

    private fun createTag(name: String): Tag {
        return try {
            tagRepository.save(Tag.create(name))
        } catch (e: DataIntegrityViolationException) {
            log.error("Tag 생성 중 오류 발생 : ${e.message}")
            tagRepository.findByName(name)
                ?: throw BusinessException(StudyServiceErrorCode.TAG_CREATION_FAIL)
        }
    }
}
