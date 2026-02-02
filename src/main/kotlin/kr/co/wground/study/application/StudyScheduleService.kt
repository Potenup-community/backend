package kr.co.wground.study.application

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.TrackId
import kr.co.wground.study.application.dto.ScheduleCreateCommand
import kr.co.wground.study.application.dto.ScheduleInfo
import kr.co.wground.study.application.dto.ScheduleUpdateCommand
import kr.co.wground.study.application.event.StudyScheduleChangedEvent
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
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.LocalTime

@Service
@Transactional
class StudyScheduleService(
    private val studyScheduleRepository: StudyScheduleRepository,
    private val trackRepository: TrackRepository,
    private val studyRepository: StudyRepository,
    private val scheduleValidator: StudyScheduleValidator,
    private val eventPublisher: ApplicationEventPublisher
) {
    fun createSchedule(command: ScheduleCreateCommand): ScheduleCreateResponse {
        val track = trackRepository.findByIdOrNull(command.trackId)
            ?: throw BusinessException(StudyServiceErrorCode.TRACK_NOT_FOUND)

        val existSchedules = studyScheduleRepository.findAllByTrackIdOrderByMonthsAsc(command.trackId)

        scheduleValidator.validate(
            newInfo = ScheduleInfo(
                month = command.month,
                recruitStart = command.recruitStartDate.atStartOfDay(),
                studyEnd = command.studyEndDate.atTime(LocalTime.MAX)
            ),
            track = track,
            existSchedules = existSchedules
        )

        val savedSchedule = studyScheduleRepository.save(command.toEntity())

        eventPublisher.publishEvent(
            StudyScheduleChangedEvent.of(
                schedule = savedSchedule,
                studyScheduleEventType = StudyScheduleChangedEvent.EventType.CREATED
            )
        )

        return ScheduleCreateResponse.of(savedSchedule.id, savedSchedule.trackId, savedSchedule.months)
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

        validateNewSchedule(command, schedule, track, existSchedules)

        schedule.updateSchedule(
            newMonths = command.months,
            newRecruitStart = command.recruitStartDate,
            newRecruitEnd = command.recruitEndDate,
            newStudyEnd = command.studyEndDate
        )

        refreshAffectedStudies(schedule)
        publishUpdateEvent(schedule)

        return ScheduleUpdateResponse.of(schedule.id, schedule.trackId, schedule.months)
    }

    fun deleteSchedule(scheduleId: Long) {
        if (studyRepository.existsByScheduleId(scheduleId)) {
            throw BusinessException(StudyServiceErrorCode.CANNOT_DELETE_SCHEDULE_WITH_STUDIES)
        }
        val schedule = getScheduleEntity(scheduleId)
        studyScheduleRepository.deleteById(scheduleId)

        eventPublisher.publishEvent(
            StudyScheduleChangedEvent.of(
                schedule = schedule,
                studyScheduleEventType = StudyScheduleChangedEvent.EventType.DELETED
            )
        )
    }

    private fun validateNewSchedule(
        command: ScheduleUpdateCommand,
        schedule: StudySchedule,
        track: Track,
        existSchedules: List<StudySchedule>
    ) {
        val newInfo = ScheduleInfo(
            month = command.months ?: schedule.months,
            recruitStart = command.recruitStartDate?.atStartOfDay() ?: schedule.recruitStartDate,
            studyEnd = command.studyEndDate?.atTime(LocalTime.MAX) ?: schedule.studyEndDate
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

    fun getTrackOrThrow(trackId: TrackId): Track{
        return trackRepository.findByIdOrNull(trackId)
            ?: throw BusinessException(StudyServiceErrorCode.TRACK_NOT_FOUND)
    }

    fun getOtherSchedules(schedule: StudySchedule): List<StudySchedule>{
        return studyScheduleRepository.findAllByTrackIdOrderByMonthsAsc(schedule.trackId)
            .filter { it.id != schedule.id }
    }

    fun publishUpdateEvent(schedule: StudySchedule){
        eventPublisher.publishEvent(
            StudyScheduleChangedEvent.of(
                schedule = schedule,
                studyScheduleEventType = StudyScheduleChangedEvent.EventType.UPDATED
            )
        )
    }
}
