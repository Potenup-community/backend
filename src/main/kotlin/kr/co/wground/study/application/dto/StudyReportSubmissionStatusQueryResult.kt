package kr.co.wground.study.application.dto

import kr.co.wground.study.domain.StudyReport
import kr.co.wground.study.domain.enums.StudyReportApprovalStatus
import java.time.LocalDateTime

data class StudyReportSubmissionStatusQueryResult(
    val hasReport: Boolean,
    val status: StudyReportApprovalStatus?,
    val submittedAt: LocalDateTime?,
    val lastModifiedAt: LocalDateTime?,
) {
    companion object {
        fun of(report: StudyReport?): StudyReportSubmissionStatusQueryResult {
            if (report == null) {
                return StudyReportSubmissionStatusQueryResult(
                    hasReport = false,
                    status = null,
                    submittedAt = null,
                    lastModifiedAt = null,
                )
            }

            return StudyReportSubmissionStatusQueryResult(
                hasReport = true,
                status = report.status,
                submittedAt = report.submittedAt,
                lastModifiedAt = report.lastModifiedAt,
            )
        }
    }
}
