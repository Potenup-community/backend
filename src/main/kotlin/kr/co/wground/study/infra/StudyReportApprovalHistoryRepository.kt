package kr.co.wground.study.infra

import kr.co.wground.study.domain.StudyReportApprovalHistory
import org.springframework.data.jpa.repository.JpaRepository

interface StudyReportApprovalHistoryRepository : JpaRepository<StudyReportApprovalHistory, Long> {
    fun findAllByStudyReportIdOrderByActedAtDesc(studyReportId: Long): List<StudyReportApprovalHistory>
}
