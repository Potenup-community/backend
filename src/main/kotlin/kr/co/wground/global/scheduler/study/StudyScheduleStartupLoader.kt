package kr.co.wground.global.scheduler.study

import kr.co.wground.study.domain.enums.StudyStatus
import kr.co.wground.study.infra.StudyRepository
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
    private val studyRepository: StudyRepository,
    private val studyScheduleTaskManager: StudyScheduleTaskManager,
    @Value("\${spring.jpa.properties.hibernate.jdbc.batch_size:100}")
    private val batchSize: Int
) {

    @EventListener(ApplicationReadyEvent::class)
    @Transactional
    fun loadSchedulerTask(event: ApplicationReadyEvent) {

        var pageNumber = 0
        val now = LocalDateTime.now()
        
        while (true) {
            val pageRequest = PageRequest.of(pageNumber, batchSize)
            val schedules = studyScheduleRepository.findAll(pageRequest)

            if (schedules.isEmpty()) break

            schedules.forEach { schedule ->

                // 이미 스터디 종료 시점이 지났으나, IN_PROGRESS 상태로 유지되고 있는 스터디의 상태를 COMPLETED 상태로 변경하거나
                // 또는 이미 모집 마감 시점이 지났으나, RECRUITING 상태로 유지되고 있는 스터디의 상태를 RECRUITING_CLOSED 로 변경한다.
                if (now.isAfter(schedule.studyEndDate)) {
                    val studies = studyRepository.findAllByScheduleId(schedule.id)
                    studies
                        .filter { it.status == StudyStatus.IN_PROGRESS }
                        .forEach { it.complete() }
                } else if (LocalDateTime.now().isAfter(schedule.recruitEndDate)) {
                } else if (now.isAfter(schedule.recruitEndDate)) {
                    studyRepository.findAllByScheduleId(schedule.id)
                        .filter { it.status == StudyStatus.RECRUITING }
                        .forEach { it.closeRecruitment() }
                }

                // 이미 모집 일정이 마감되었거나, 스터디 일정이 종료된 스터디의 경우 태스크가 추가되지 않음
                studyScheduleTaskManager.addTask(
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
