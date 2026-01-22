package kr.co.wground.study.application

import kr.co.wground.exception.BusinessException
import kr.co.wground.study.application.dto.ScheduleCreateCommand
import kr.co.wground.study.application.dto.ScheduleUpdateCommand
import kr.co.wground.study.application.exception.StudyServiceErrorCode
import kr.co.wground.study.infra.StudyScheduleRepository
import kr.co.wground.study.presentation.response.ScheduleCreateResponse
import kr.co.wground.study.presentation.response.ScheduleQueryResponse
import kr.co.wground.study.presentation.response.ScheduleUpdateResponse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class StudyScheduleService(
    private val studyScheduleRepository: StudyScheduleRepository
) {
    fun createSchedule(command: ScheduleCreateCommand): ScheduleCreateResponse {
        val savedSchedule = studyScheduleRepository.save(command.toEntity())
        return ScheduleCreateResponse.of(savedSchedule.id, savedSchedule.trackId, savedSchedule.months)
    }

    fun updateSchedule(command: ScheduleUpdateCommand): ScheduleUpdateResponse {
        val schedule = studyScheduleRepository.findByIdOrNull(command.id) ?: throw BusinessException(
            StudyServiceErrorCode.STUDY_SCHEDULE_IS_NOT_IN_TRACK)

        schedule.updateSchedule(
            newMonths = command.months,
            newRecruitStart = command.recruitStartDate,
            newRecruitEnd = command.recruitEndDate,
            newStudyEnd = command.studyEndDate
        )

        return ScheduleUpdateResponse.of(schedule.id, schedule.trackId, schedule.months)
    }

    fun deleteSchedule(scheduleId: Long) {
        studyScheduleRepository.deleteById(scheduleId)
    }

    fun getAllSchedules() :List<ScheduleQueryResponse>{
        studyScheduleRepository.findAll()
    }

}