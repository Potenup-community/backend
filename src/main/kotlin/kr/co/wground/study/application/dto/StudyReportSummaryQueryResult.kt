package kr.co.wground.study.application.dto

import kr.co.wground.global.common.UserId
import kr.co.wground.study.domain.StudyReport
import kr.co.wground.study.domain.enums.StudyReportApprovalStatus
import java.time.LocalDateTime

data class StudyReportSummaryQueryResult(
    val reportId: Long,
    val studyId: Long,
    val studyName: String,
    val leaderId: UserId,
    val leaderName: String,
    val status: StudyReportApprovalStatus,
    val submittedAt: LocalDateTime,
    val lastModifiedAt: LocalDateTime,
) {
    companion object {
        fun of(report: StudyReport, leaderName: String): StudyReportSummaryQueryResult {
            return StudyReportSummaryQueryResult(
                reportId = report.id,
                studyId = report.study.id,
                studyName = report.study.name,
                leaderId = report.study.leaderId,
                leaderName = leaderName,
                status = report.status,
                submittedAt = report.submittedAt,
                lastModifiedAt = report.lastModifiedAt,
            )
        }
    }
}
