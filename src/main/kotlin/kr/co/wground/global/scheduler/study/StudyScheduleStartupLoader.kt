package kr.co.wground.global.scheduler.study

import kr.co.wground.study_schedule.infra.StudyScheduleRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class StudyScheduleStartupLoader(
    private val studyScheduleRepository: StudyScheduleRepository,
    private val studySchedulerManager: StudySchedulerManager,
    @Value("\${spring.jpa.properties.hibernate.jdbc.batch_size:100}")
    private val batchSize: Int
) {
    @EventListener(ApplicationReadyEvent::class)
    @Transactional(readOnly = true)
    fun loadSchedulerTask(event: ApplicationReadyEvent) {
        var pageNumber = 0
        val now = LocalDateTime.now()
        
        while (true) {
            val pageRequest = PageRequest.of(pageNumber, batchSize)
            val schedules = studyScheduleRepository.findAllByStudyEndDateAfter(now, pageRequest)

            if (schedules.isEmpty()) break

            schedules.forEach { schedule ->
                studySchedulerManager.addTask(
                    scheduleId = schedule.id,
                    recruitStart = schedule.recruitStartDate,
                    recruitEnd = schedule.recruitEndDate,
                    studyEnd = schedule.studyEndDate
                )
            }
            pageNumber++
        }
    }
}
