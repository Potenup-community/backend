package kr.co.wground.study.application

import java.time.LocalDateTime
import java.time.LocalTime
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.TrackId
import kr.co.wground.global.common.UserId
import kr.co.wground.study.application.dto.ScheduleCreateCommand
import kr.co.wground.study.application.dto.ScheduleInfo
import kr.co.wground.study.application.dto.ScheduleUpdateCommand
import kr.co.wground.study.application.dto.QueryStudyScheduleDto
import kr.co.wground.study.application.exception.StudyServiceErrorCode
import kr.co.wground.study.domain.StudySchedule
import kr.co.wground.study.infra.StudyRepository
import kr.co.wground.study.infra.StudyScheduleRepository
import kr.co.wground.study.presentation.response.schedule.ScheduleCreateResponse
import kr.co.wground.study.presentation.response.schedule.ScheduleQueryResponse
import kr.co.wground.study.presentation.response.schedule.ScheduleUpdateResponse
import kr.co.wground.track.infra.TrackRepository
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.infra.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class StudyScheduleService(
    private val studyScheduleRepository: StudyScheduleRepository,
    private val userRepository: UserRepository,
    private val trackRepository: TrackRepository,
    private val studyRepository: StudyRepository,
    private val scheduleValidator: StudyScheduleValidator
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

        val track = trackRepository.findByIdOrNull(schedule.trackId)
            ?: throw BusinessException(StudyServiceErrorCode.TRACK_NOT_FOUND)
        val existSchedules = studyScheduleRepository.findAllByTrackIdOrderByMonthsAsc(schedule.trackId)
            .filter { it.id != schedule.id }

        val newRecruitStart = command.recruitStartDate?.atStartOfDay() ?: schedule.recruitStartDate
        val newStudyEnd = command.studyEndDate?.atTime(LocalTime.MAX) ?: schedule.studyEndDate
        val newMonth = command.months ?: schedule.months

        scheduleValidator.validate(
            newInfo = ScheduleInfo(
                month = newMonth,
                recruitStart = newRecruitStart,
                studyEnd = newStudyEnd
            ),
            track = track,
            existSchedules = existSchedules
        )

        schedule.updateSchedule(
            newMonths = command.months,
            newRecruitStart = command.recruitStartDate,
            newRecruitEnd = command.recruitEndDate,
            newStudyEnd = command.studyEndDate
        )

        val affectedStudies = studyRepository.findAllByScheduleId(schedule.id)
        affectedStudies.forEach { study ->
            study.refreshStatus(schedule.isRecruitmentClosed())
        }

        return ScheduleUpdateResponse.of(schedule.id, schedule.trackId, schedule.months)
    }

    fun deleteSchedule(scheduleId: Long) {
        if (studyRepository.existsByScheduleId(scheduleId)) {
            throw BusinessException(StudyServiceErrorCode.CANNOT_DELETE_SCHEDULE_WITH_STUDIES)
        }
        studyScheduleRepository.deleteById(scheduleId)
    }

    fun getScheduleEntity(id: Long): StudySchedule {
        return studyScheduleRepository.findByIdOrNull(id)
            ?: throw BusinessException(StudyServiceErrorCode.SCHEDULE_NOT_FOUND)
    }

    fun getSchedules(userId: UserId): List<QueryStudyScheduleDto> {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

        return studyScheduleRepository.findAllByTrackIdOrderByMonthsAsc(user.trackId).map { QueryStudyScheduleDto.from(it) }
    }

    fun getUserTrackId(userId: UserId): TrackId{
        val user = userRepository.findByIdOrNull(userId) ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)
        return user.trackId
    }
}
