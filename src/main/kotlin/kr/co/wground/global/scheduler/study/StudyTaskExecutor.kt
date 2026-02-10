package kr.co.wground.global.scheduler.study

import kr.co.wground.common.event.StudyEndedEvent
import kr.co.wground.common.event.StudyRecruitEndedEvent
import kr.co.wground.common.event.StudyRecruitStartedEvent
import kr.co.wground.study_schedule.infra.StudyScheduleRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class StudyTaskExecutor(
    private val studyScheduleRepository: StudyScheduleRepository,
    private val eventPublisher: ApplicationEventPublisher
) {
    @Transactional(readOnly = true)
    fun executeRecruitStart(scheduleId: Long) {
        val schedule = studyScheduleRepository.findByIdOrNull(scheduleId) ?: return
        eventPublisher.publishEvent(
            StudyRecruitStartedEvent(
                scheduleId = schedule.id,
                trackId = schedule.trackId,
                months = schedule.months
            )
        )
    }

    @Transactional(readOnly = true)
    fun executeRecruitEnd(scheduleId: Long) {
        val schedule = studyScheduleRepository.findByIdOrNull(scheduleId) ?: return
        eventPublisher.publishEvent(
            StudyRecruitEndedEvent(
                scheduleId = schedule.id,
                trackId = schedule.trackId,
                months = schedule.months
            )
        )
    }

    @Transactional(readOnly = true)
    fun executeStudyEnd(scheduleId: Long) {
        val schedule = studyScheduleRepository.findByIdOrNull(scheduleId) ?: return
        eventPublisher.publishEvent(
            StudyEndedEvent(
                scheduleId = schedule.id,
                trackId = schedule.trackId,
                months = schedule.months
            )
        )
    }
}
