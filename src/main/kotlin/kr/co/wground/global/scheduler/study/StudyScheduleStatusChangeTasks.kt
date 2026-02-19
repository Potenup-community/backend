package kr.co.wground.global.scheduler.study

import kr.co.wground.study.infra.StudyRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class StudyScheduleStatusChangeTasks(
    private val studyRepository: StudyRepository
) {

    @Transactional
    fun closeStudyRecruitment(scheduleId: Long) {
        val studies = studyRepository.findAllByScheduleId(scheduleId)
        studies.forEach { it.close() }
    }
}