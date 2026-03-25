package kr.co.wground.study.presentation.response.study_report

import kr.co.wground.global.common.UserId
import kr.co.wground.study.application.dto.StudyReportSummaryQueryResult
import kr.co.wground.study.domain.enums.StudyReportApprovalStatus
import java.time.LocalDateTime

data class StudyReportSummaryResponse(
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
        fun from(queryResult: StudyReportSummaryQueryResult): StudyReportSummaryResponse {
            return StudyReportSummaryResponse(
                reportId = queryResult.reportId,
                studyId = queryResult.studyId,
                studyName = queryResult.studyName,
                leaderId = queryResult.leaderId,
                leaderName = queryResult.leaderName,
                status = queryResult.status,
                submittedAt = queryResult.submittedAt,
                lastModifiedAt = queryResult.lastModifiedAt,
            )
        }
    }
}
