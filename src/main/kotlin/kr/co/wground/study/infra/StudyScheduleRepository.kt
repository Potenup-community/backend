package kr.co.wground.study.infra

import kr.co.wground.study.domain.StudySchedule
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface StudyScheduleRepository : JpaRepository<StudySchedule, Long> {
    fun save(studySchedule: StudySchedule): StudySchedule
    fun findAllByTrackIdOrderByMonthsAsc(trackId: Long): List<StudySchedule>
    fun findAllByStudyEndDateAfter(studyEndDate: LocalDateTime, pageable: Pageable): List<StudySchedule>
}