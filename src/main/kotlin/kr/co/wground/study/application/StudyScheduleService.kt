package kr.co.wground.study.application

import java.time.LocalDate
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.TrackId
import kr.co.wground.global.common.UserId
import kr.co.wground.study.application.dto.ScheduleCreateCommand
import kr.co.wground.study.application.dto.ScheduleInfo
import kr.co.wground.study.application.dto.ScheduleUpdateCommand
import kr.co.wground.study.application.event.StudyScheduleChangedEvent
import kr.co.wground.study.application.dto.QueryStudyScheduleDto
import kr.co.wground.study.application.exception.StudyServiceErrorCode
import kr.co.wground.study.domain.StudySchedule
import kr.co.wground.study.infra.StudyRepository
import kr.co.wground.study.infra.StudyScheduleRepository
import kr.co.wground.study.presentation.response.schedule.ScheduleCreateResponse
import kr.co.wground.study.presentation.response.schedule.ScheduleQueryResponse
import kr.co.wground.study.presentation.response.schedule.ScheduleUpdateResponse
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
import kr.co.wground.study.domain.constant.Months

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

        publishUpdateEvent(savedSchedule, StudyScheduleChangedEvent.EventType.CREATED)

        return ScheduleCreateResponse.of(savedSchedule.id, savedSchedule.trackId, savedSchedule.months)
    }

    @Transactional(readOnly = true)
    fun getCurrentScheduleByUserId(userId: UserId): ScheduleQueryResponse? {
        val trackId = getUserTrackId(userId)
        return getCurrentSchedule(trackId)
    }

    @Transactional(readOnly = true)
    fun getCurrentSchedule(trackId: Long): ScheduleQueryResponse? {
        val now = LocalDateTime.now()
        return studyScheduleRepository.findAllByTrackIdOrderByMonthsAsc(trackId)
            .firstOrNull { it.isCurrentRound(now) }
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
    }

    fun updateSchedule(command: ScheduleUpdateCommand): ScheduleUpdateResponse {
        val schedule = getScheduleEntity(command.id)
        val track = getTrackOrThrow(schedule.trackId)
        val existSchedules = getOtherSchedules(schedule)

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
        publishUpdateEvent(schedule, StudyScheduleChangedEvent.EventType.UPDATED)

        return ScheduleUpdateResponse.of(schedule.id, schedule.trackId, schedule.months)
    }

    fun deleteSchedule(scheduleId: Long) {
        if (studyRepository.existsByScheduleId(scheduleId)) {
            throw BusinessException(StudyServiceErrorCode.CANNOT_DELETE_SCHEDULE_WITH_STUDIES)
        }
        val schedule = getScheduleEntity(scheduleId)

        studyScheduleRepository.deleteById(scheduleId)

        publishUpdateEvent(schedule, StudyScheduleChangedEvent.EventType.DELETED)
    }

    fun getSchedulesByUserId(userId: UserId): List<QueryStudyScheduleDto> {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

        return studyScheduleRepository.findAllByTrackIdOrderByMonthsAsc(user.trackId).map { QueryStudyScheduleDto.from(it) }
    }

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
            ?: throw BusinessException(StudyServiceErrorCode.INVALID_SCHEDULE_PARAMETER),

            recruitStart = recruitStartDate?.atStartOfDay() ?: currentSchedule?.recruitStartDate
            ?: throw BusinessException(StudyServiceErrorCode.INVALID_SCHEDULE_PARAMETER),

            studyEnd = studyEndDate?.atTime(LocalTime.MAX) ?: currentSchedule?.studyEndDate
            ?: throw BusinessException(StudyServiceErrorCode.INVALID_SCHEDULE_PARAMETER)
        )

        scheduleValidator.validate(newInfo, track, existSchedules)
    }

    private fun refreshAffectedStudies(schedule: StudySchedule) {
        val affectedStudies = studyRepository.findAllByScheduleId(schedule.id)
        affectedStudies.forEach { it.refreshStatus(schedule.isRecruitmentClosed()) }
    }

    fun getScheduleEntity(id: Long): StudySchedule {
        return studyScheduleRepository.findByIdOrNull(id)
            ?: throw BusinessException(StudyServiceErrorCode.SCHEDULE_NOT_FOUND)
    }

    private fun getTrackOrThrow(trackId: TrackId): Track{
        return trackRepository.findByIdOrNull(trackId)
            ?: throw BusinessException(StudyServiceErrorCode.TRACK_NOT_FOUND)
    }

    private fun getOtherSchedules(schedule: StudySchedule): List<StudySchedule>{
        return studyScheduleRepository.findAllByTrackIdOrderByMonthsAsc(schedule.trackId)
            .filter { it.id != schedule.id }
    }

    private fun publishUpdateEvent(schedule: StudySchedule, eventType: StudyScheduleChangedEvent.EventType){
        eventPublisher.publishEvent(
            StudyScheduleChangedEvent.of(
                schedule = schedule,
                studyScheduleEventType = eventType
            )
        )
    }

    private fun getUserTrackId(userId: UserId): TrackId{
        val user = userRepository.findByIdOrNull(userId) ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)
        return user.trackId
    }
}
