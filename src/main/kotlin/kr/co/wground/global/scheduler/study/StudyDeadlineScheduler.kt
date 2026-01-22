package kr.co.wground.global.scheduler.study

import java.time.LocalDateTime
import kr.co.wground.study.infra.StudyRepository
import kr.co.wground.study.infra.StudyScheduleRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class StudyDeadlineScheduler(
    private val studyScheduleRepository: StudyScheduleRepository,
    private val studyRepository: StudyRepository
) {
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    fun checkDeadlines() {
        val now = LocalDateTime.now()
        val recentClosedSchedules = studyScheduleRepository.findAllByRecruitEndDateBefore(now)

        for (schedule in recentClosedSchedules) {
            val studies = studyRepository.findAllByScheduleId(schedule.id)
            studies.forEach { it.refreshStatus(now) }
        }
    }
}