package kr.co.wground.study_schedule.application

import java.time.LocalDate
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.TrackId
import kr.co.wground.global.common.UserId
import kr.co.wground.study_schedule.application.dto.ScheduleCreateCommand
import kr.co.wground.study_schedule.application.dto.ScheduleInfo
import kr.co.wground.study_schedule.application.dto.ScheduleUpdateCommand
import kr.co.wground.study_schedule.application.event.StudyScheduleCreatedOrChangedEvent
import kr.co.wground.study_schedule.application.dto.QueryStudyScheduleDto
import kr.co.wground.study.application.exception.StudyServiceErrorCode
import kr.co.wground.study_schedule.domain.StudySchedule
import kr.co.wground.study.infra.StudyRepository
import kr.co.wground.study_schedule.application.exception.StudyScheduleServiceErrorCode
import kr.co.wground.study_schedule.infra.StudyScheduleRepository
import kr.co.wground.study_schedule.presentation.response.ScheduleCreateResponse
import kr.co.wground.study_schedule.presentation.response.ScheduleQueryResponse
import kr.co.wground.study_schedule.presentation.response.ScheduleUpdateResponse
import kr.co.wground.track.domain.Track
import kr.co.wground.track.infra.TrackRepository
import org.springframework.context.ApplicationEventPublisher
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.infra.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.LocalTime
import kr.co.wground.study_schedule.domain.enums.Months
import kr.co.wground.track.domain.constant.TrackStatus

@Service
@Transactional
class StudyScheduleService(
    private val studyScheduleRepository: StudyScheduleRepository,
    private val userRepository: UserRepository,
    private val trackRepository: TrackRepository,
    private val studyRepository: StudyRepository,
    private val scheduleValidator: StudyScheduleValidator,
    private val eventPublisher: ApplicationEventPublisher
) {
    fun createSchedule(command: ScheduleCreateCommand): ScheduleCreateResponse {

        val track = trackRepository.findByIdOrNull(command.trackId)
            ?: throw BusinessException(StudyServiceErrorCode.TRACK_NOT_FOUND)

        val existSchedules = studyScheduleRepository.findAllByTrackIdOrderByMonthsAsc(command.trackId)

        validateSchedule(
            month = command.month,
            recruitStartDate = command.recruitStartDate,
            studyEndDate = command.studyEndDate,
            track = track,
            existSchedules = existSchedules
        )

        val savedSchedule = studyScheduleRepository.save(command.toEntity())

        publishUpdateEvent(savedSchedule, StudyScheduleCreatedOrChangedEvent.EventType.CREATED)

        return ScheduleCreateResponse.of(savedSchedule.id, savedSchedule.trackId, savedSchedule.months)
    }

    fun updateSchedule(command: ScheduleUpdateCommand): ScheduleUpdateResponse {
        val schedule = getScheduleById(command.id)
        val track = findTrackByIdOrThrows(schedule.trackId)
        val existSchedules = findOtherSchedules(schedule)

        validateSchedule(
            month = command.months,
            recruitStartDate = command.recruitStartDate,
            studyEndDate = command.studyEndDate,
            track = track,
            existSchedules = existSchedules,
            currentSchedule = schedule
        )

        schedule.updateSchedule(
            newMonths = command.months,
            newRecruitStart = command.recruitStartDate,
            newRecruitEnd = command.recruitEndDate,
            newStudyEnd = command.studyEndDate
        )

        refreshAffectedStudies(schedule)
        publishUpdateEvent(schedule, StudyScheduleCreatedOrChangedEvent.EventType.UPDATED)

        return ScheduleUpdateResponse.of(schedule.id, schedule.trackId, schedule.months)
    }

    fun deleteSchedule(scheduleId: Long) {
        if (studyRepository.existsByScheduleId(scheduleId)) {
            throw BusinessException(StudyScheduleServiceErrorCode.CANNOT_DELETE_SCHEDULE_WITH_STUDIES)
        }
        val schedule = getScheduleById(scheduleId)

        studyScheduleRepository.deleteById(scheduleId)

        publishUpdateEvent(schedule, StudyScheduleCreatedOrChangedEvent.EventType.DELETED)
    }

    @Transactional(readOnly = true)
    fun getAllSchedulesInTrackIds(trackIds: Set<TrackId>): Map<TrackId, List<ScheduleQueryResponse>> {

        if (trackIds.isEmpty()) {
            return emptyMap()
        }

        val foundSchedules: List<StudySchedule> = studyScheduleRepository.findAllByTrackIdIn(trackIds)
        val groupedSchedules: Map<TrackId, List<StudySchedule>> = foundSchedules.groupBy { it.trackId }
        return trackIds.associateWith { trackIds ->
            groupedSchedules[trackIds]
                .orEmpty()
                .map { ScheduleQueryResponse.from(it) }
        }
    }

    @Transactional(readOnly = true)
    fun getAllSchedulesOfEnrolledTracks(): Map<TrackId, List<ScheduleQueryResponse>> {
        val enrolledTracks: List<Track> = trackRepository.findAllByTrackStatus(TrackStatus.ENROLLED)
        val enrolledTrackIds = enrolledTracks.map { it.trackId }.toSet()
        return getAllSchedulesInTrackIds(enrolledTrackIds)
    }

    @Transactional(readOnly = true)
    fun getCurrentScheduleByUserId(userId: UserId): ScheduleQueryResponse? {
        val trackId = findTrackIdByUserIdOrThrows(userId)
        return getCurrentSchedule(trackId)
    }

    @Transactional(readOnly = true)
    fun getCurrentSchedule(trackId: Long): ScheduleQueryResponse {
        val now = LocalDateTime.now()
        return studyScheduleRepository.findAllByTrackIdOrderByMonthsAsc(trackId)
            .firstOrNull { it.isCurrentMonth(now) }
            ?.let {
                ScheduleQueryResponse(
                    id = it.id,
                    trackId = it.trackId,
                    months = it.months,
                    monthName = it.months.month,
                    recruitStartDate = it.recruitStartDate,
                    recruitEndDate = it.recruitEndDate,
                    studyEndDate = it.studyEndDate
                )
            }
            ?: throw BusinessException(StudyScheduleServiceErrorCode.NO_CURRENT_SCHEDULE)
    }

    @Transactional(readOnly = true)
    fun getAllSchedulesByTrackOfTheUser(userId: UserId): List<QueryStudyScheduleDto> {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

        return studyScheduleRepository.findAllByTrackIdOrderByMonthsAsc(user.trackId).map { QueryStudyScheduleDto.from(it) }
    }

    @Transactional(readOnly = true)
    fun getScheduleById(id: Long): StudySchedule {
        return studyScheduleRepository.findByIdOrNull(id)
            ?: throw BusinessException(StudyScheduleServiceErrorCode.SCHEDULE_NOT_FOUND)
    }

    // ----- helpers

    private fun validateSchedule(
        month: Months?,
        recruitStartDate: LocalDate?,
        studyEndDate: LocalDate?,
        track: Track,
        existSchedules: List<StudySchedule>,
        currentSchedule: StudySchedule? = null
    ) {
        val newInfo = ScheduleInfo(
            month = month ?: currentSchedule?.months
                    ?: throw BusinessException(StudyScheduleServiceErrorCode.INVALID_SCHEDULE_PARAMETER),

            recruitStart = recruitStartDate?.atStartOfDay() ?: currentSchedule?.recruitStartDate
                    ?: throw BusinessException(StudyScheduleServiceErrorCode.INVALID_SCHEDULE_PARAMETER),

            studyEnd = studyEndDate?.atTime(LocalTime.MAX) ?: currentSchedule?.studyEndDate
                    ?: throw BusinessException(StudyScheduleServiceErrorCode.INVALID_SCHEDULE_PARAMETER)
        )

        scheduleValidator.validate(newInfo, track, existSchedules)
    }

    private fun refreshAffectedStudies(schedule: StudySchedule) {
        val affectedStudies = studyRepository.findAllByScheduleId(schedule.id)
        if (LocalDateTime.now().isAfter(schedule.studyEndDate)) {
            affectedStudies.forEach {
                // RECRUITING -> RECRUITING_CLOSED
                it.closeRecruitment()
                // IN_PROGRESS -> COMPLETED
                it.complete()
            }
        } else if (LocalDateTime.now().isAfter(schedule.recruitEndDate)) {
            affectedStudies.forEach { it.closeRecruitment() }
        }
    }

    private fun findTrackByIdOrThrows(trackId: TrackId): Track{
        return trackRepository.findByIdOrNull(trackId)
            ?: throw BusinessException(StudyServiceErrorCode.TRACK_NOT_FOUND)
    }

    private fun findOtherSchedules(schedule: StudySchedule): List<StudySchedule>{
        return studyScheduleRepository.findAllByTrackIdOrderByMonthsAsc(schedule.trackId)
            .filter { it.id != schedule.id }
    }

    private fun findTrackIdByUserIdOrThrows(userId: UserId): TrackId {
        val user = userRepository.findByIdOrNull(userId)
                ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)
        return user.trackId
    }

    private fun publishUpdateEvent(schedule: StudySchedule, eventType: StudyScheduleCreatedOrChangedEvent.EventType){
        eventPublisher.publishEvent(
            StudyScheduleCreatedOrChangedEvent.of(
                schedule = schedule,
                studyScheduleEventType = eventType
            )
        )
    }
}
