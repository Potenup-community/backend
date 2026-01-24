package kr.co.wground.study.infra

import kr.co.wground.study.domain.StudySchedule
import org.springframework.data.jpa.repository.JpaRepository

interface StudyScheduleRepository : JpaRepository<StudySchedule, Long> {
    fun save(studySchedule: StudySchedule): StudySchedule
    fun findAllByTrackIdOrderByMonthsAsc(trackId: Long): List<StudySchedule>
}