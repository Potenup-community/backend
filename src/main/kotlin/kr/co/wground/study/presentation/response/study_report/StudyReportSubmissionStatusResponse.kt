package kr.co.wground.study.presentation.response.study_report

import kr.co.wground.study.application.dto.StudyReportSubmissionStatusQueryResult
import kr.co.wground.study.domain.enums.StudyReportApprovalStatus
import java.time.LocalDateTime

data class StudyReportSubmissionStatusResponse(
    val hasReport: Boolean,
    val status: StudyReportApprovalStatus?,
    val submittedAt: LocalDateTime?,
    val lastModifiedAt: LocalDateTime?,
) {
    companion object {
        fun from(queryResult: StudyReportSubmissionStatusQueryResult): StudyReportSubmissionStatusResponse {
            return StudyReportSubmissionStatusResponse(
                hasReport = queryResult.hasReport,
                status = queryResult.status,
                submittedAt = queryResult.submittedAt,
                lastModifiedAt = queryResult.lastModifiedAt,
            )
        }
    }
}
