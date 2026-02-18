package kr.co.wground.study.infra

import kr.co.wground.study.domain.StudyReport
import org.springframework.data.jpa.repository.JpaRepository

interface StudyReportRepository : JpaRepository<StudyReport, Long> {
    fun findByStudyId(studyId: Long): StudyReport?
    fun existsByStudyId(studyId: Long): Boolean
}
