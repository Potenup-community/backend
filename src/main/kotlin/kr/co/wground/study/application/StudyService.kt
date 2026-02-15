package kr.co.wground.study.application

import kr.co.wground.common.event.StudyDeletedEvent
import kr.co.wground.exception.BusinessException
import kr.co.wground.study.application.dto.ParticipantInfo
import kr.co.wground.study_schedule.application.dto.ScheduleDto
import kr.co.wground.study.application.dto.StudyCreateCommand
import kr.co.wground.study.application.dto.StudySearchDto
import kr.co.wground.study.application.dto.StudyUpdateCommand
import kr.co.wground.study.application.exception.StudyServiceErrorCode
import kr.co.wground.study.domain.Study
import kr.co.wground.study.domain.Tag
import kr.co.wground.study.infra.StudyRecruitmentRepository
import kr.co.wground.study.infra.StudyRepository
import kr.co.wground.study.infra.TagRepository
import kr.co.wground.study.presentation.response.study.StudyDetailResponse
import kr.co.wground.study.presentation.response.study.StudySearchResponse
import kr.co.wground.study_schedule.application.StudyScheduleService
import kr.co.wground.study_schedule.application.exception.StudyScheduleServiceErrorCode
import kr.co.wground.track.domain.constant.TrackStatus
import kr.co.wground.track.infra.TrackRepository
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.infra.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Slice
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class StudyService(
    private val studyRepository: StudyRepository,
    private val studyScheduleService: StudyScheduleService,
    private val studyRecruitmentRepository: StudyRecruitmentRepository,
    private val trackRepository: TrackRepository,
    private val tagRepository: TagRepository,
    private val userRepository: UserRepository,
    private val eventPublisher: ApplicationEventPublisher,
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

        val isRecruitDueOver = schedule.recruitEndDate.isBefore(LocalDateTime.now())
        if (isRecruitDueOver) {
            throw BusinessException(StudyScheduleServiceErrorCode.RECRUIT_DUE_IS_OVER)
        }

        val enrolledStudyCount = studyRecruitmentRepository.countStudyRecruitment(user.userId, schedule.id)
        if (enrolledStudyCount >= MAX_ENROLLED_STUDY) {
            throw BusinessException(StudyServiceErrorCode.MAX_STUDY_EXCEEDED)
        }

        val tags = resolveTags(command.tags)

        val scheduleEntity = studyScheduleService.getScheduleById(schedule.id)

        val study = Study.createNew(
            name = command.name,
            leaderId = user.userId,
            trackId = track.trackId,
            scheduleId = scheduleEntity.id,
            description = command.description,
            capacity = command.capacity,
            budget = command.budget,
            budgetExplain = command.budgetExplain,
            externalChatUrl = command.chatUrl,
            referenceUrl = command.refUrl
        )
        tags.forEach { study.addTag(it) }
        val savedStudy = studyRepository.save(study)

        return savedStudy.id
    }

    fun updateStudy(command: StudyUpdateCommand): Long {

        val study = findStudyEntityOrThrows(command.studyId)
        
        // 스케쥴이 없으면 예외 발생함
        studyScheduleService.getScheduleById(study.scheduleId)

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
            newBudgetExplain = command.budgetExplain ?: study.budgetExplain,
            newChatUrl = command.chatUrl ?: study.externalChatUrl,
            newRefUrl = command.refUrl ?: study.referenceUrl,
            newTags = newTags,
        )
        return study.id
    }

    fun deleteStudy(studyId: Long, userId: Long, isAdmin: Boolean) {

        val study = findStudyEntityOrThrows(studyId)

        if (!isAdmin && !study.isLeader(userId)) {
            throw BusinessException(StudyServiceErrorCode.ONLY_ADMIN_AND_LEADER_COULD_DELETE_STUDY)
        }
        study.validateHardDeletable()

        val studyId = study.id
        val recruitIds = study.recruitments.map { recruitment -> recruitment.userId }
        val studyName = study.name

        studyRepository.delete(study)

        // 스터디 삭제 이벤트가 필요할까?
        eventPublisher.publishEvent(
            StudyDeletedEvent(
                studyId = studyId,
                studyTitle = studyName,
                userIds = recruitIds
            )
        )
    }

    fun approveStudy(studyId: Long) {
        val study = findStudyEntityOrThrows(studyId)
        study.approve()
    }

    @Transactional(readOnly = true)
    fun searchStudies(
        condition: StudySearchDto
    ): Slice<StudySearchResponse> {
        val userId = condition.userId
        val result = studyRepository.searchStudies(condition.condition, condition.pageable, condition.sortType)

        return result.map { result ->
            val leaderInfo = ParticipantInfo(
                result.leader.userId,
                result.leader.name,
                result.track.trackId,
                result.track.trackName,
                result.study.recruitments.first{ it.userId == result.leader.userId }.createdAt,
                result.leader.accessProfile()
            )

            StudySearchResponse.of(
                study = result.study,
                userId = userId,
                leaderInfo = leaderInfo,
            )
        }
    }

    @Transactional(readOnly = true)
    fun getStudy(studyId: Long, userId: Long): StudyDetailResponse {
        val study = findStudyEntityOrThrows(studyId)
        val schedule = studyScheduleService.getScheduleById(study.scheduleId)
        val track = trackRepository.findByIdOrNull(study.trackId)
            ?: throw BusinessException(StudyServiceErrorCode.TRACK_NOT_FOUND)

        val participants = userRepository.findByUserIdIn(study.recruitments.map { it.userId })
        val participantInfoList = participants.map {
                ParticipantInfo(
                    id = it.userId,
                    name = it.name,
                    trackId = track.trackId,
                    trackName = track.trackName,
                    joinedAt = study.recruitments.first{ it.userId == it.userId }.createdAt,
                    profileImageUrl = it.accessProfile()
                )
            }

        return StudyDetailResponse.of(
            study = study,
            userId = userId,
            schedule = schedule,
            participants = participantInfoList
        )
    }

    // ----- helpers

    private fun findStudyEntityOrThrows(id: Long): Study {
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
