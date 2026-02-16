package kr.co.wground.global.scheduler.study

import kr.co.wground.common.event.StudyEndedSoonEvent
import kr.co.wground.common.event.StudyRecruitEndedSoonEvent
import kr.co.wground.common.event.StudyRecruitStartedEvent
import kr.co.wground.study_schedule.infra.StudyScheduleRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class StudyScheduleNotificationTasks(
    private val studyScheduleRepository: StudyScheduleRepository,
    private val eventPublisher: ApplicationEventPublisher
) {

    @Transactional(readOnly = true)
    fun publishStudyRecruitStartedEvent(scheduleId: Long) {

        val schedule = studyScheduleRepository.findByIdOrNull(scheduleId) ?: return
        eventPublisher.publishEvent(
            StudyRecruitStartedEvent(
                scheduleId = schedule.id,
                trackId = schedule.trackId,
                months = schedule.months,
                studyRecruitStartedAt = schedule.recruitStartDate
            )
        )
    }

    @Transactional(readOnly = true)
    fun publishStudyRecruitEndedSoonEvent(scheduleId: Long) {

        val schedule = studyScheduleRepository.findByIdOrNull(scheduleId) ?: return
        eventPublisher.publishEvent(
            StudyRecruitEndedSoonEvent(
                scheduleId = schedule.id,
                trackId = schedule.trackId,
                months = schedule.months,
                studyRecruitWillBeEndedAt = schedule.recruitEndDate
            )
        )
    }

    @Transactional(readOnly = true)
    fun publishStudyEndedSoonEvent(scheduleId: Long) {

        val schedule = studyScheduleRepository.findByIdOrNull(scheduleId) ?: return
        eventPublisher.publishEvent(
            StudyEndedSoonEvent(
                scheduleId = schedule.id,
                trackId = schedule.trackId,
                months = schedule.months,
                studyWillBeEndedAt = schedule.studyEndDate
            )
        )
    }
}
