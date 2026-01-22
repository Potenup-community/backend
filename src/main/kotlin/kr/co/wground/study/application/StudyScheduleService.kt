package kr.co.wground.study.application

import kr.co.wground.exception.BusinessException
import kr.co.wground.study.application.dto.ScheduleCreateCommand
import kr.co.wground.study.application.dto.ScheduleUpdateCommand
import kr.co.wground.study.infra.StudyScheduleRepository
import kr.co.wground.study.presentation.response.ScheduleCreateResponse
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

    fun updateSchedule(command: ScheduleUpdateCommand) {
        val schedule = studyScheduleRepository.findByIdOrNull(command.id) ?: throw BusinessException()
    }

}