package kr.co.wground.global.scheduler.study

import kr.co.wground.study_schedule.application.event.StudyScheduleChangedEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class StudyScheduleEventListener(
    private val studySchedulerManager: StudySchedulerManager
) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleStudyScheduleChangedEvent(event: StudyScheduleChangedEvent) {

        when (event.type) {
            StudyScheduleChangedEvent.EventType.CREATED,
            StudyScheduleChangedEvent.EventType.UPDATED -> {
                studySchedulerManager.addTask(
                    scheduleId = event.scheduleId,
                    recruitStart = event.recruitStartDate,
                    recruitEnd = event.recruitEndDate,
                    studyEnd = event.studyEndDate
                )
            }

            StudyScheduleChangedEvent.EventType.DELETED -> {
                studySchedulerManager.removeTask(event.scheduleId)
            }
        }
    }
}
