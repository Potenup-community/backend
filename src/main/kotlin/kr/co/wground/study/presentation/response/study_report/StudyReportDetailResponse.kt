package kr.co.wground.study.presentation.response.study_report

import kr.co.wground.study.application.dto.StudyReportDetailQueryResult
import kr.co.wground.study.domain.enums.StudyReportApprovalStatus
import java.time.LocalDateTime

data class StudyReportDetailResponse(
    val reportId: Long,
    val studyId: Long,
    val status: StudyReportApprovalStatus,
    val week1Activity: String,
    val week2Activity: String,
    val week3Activity: String,
    val week4Activity: String,
    val retrospectiveGood: String,
    val retrospectiveImprove: String,
    val retrospectiveNextAction: String,
    val submittedAt: LocalDateTime,
    val lastModifiedAt: LocalDateTime,
) {
    companion object {
        fun from(queryResult: StudyReportDetailQueryResult): StudyReportDetailResponse {
            return StudyReportDetailResponse(
                reportId = queryResult.reportId,
                studyId = queryResult.studyId,
                status = queryResult.status,
                week1Activity = queryResult.week1Activity,
                week2Activity = queryResult.week2Activity,
                week3Activity = queryResult.week3Activity,
                week4Activity = queryResult.week4Activity,
                retrospectiveGood = queryResult.retrospectiveGood,
                retrospectiveImprove = queryResult.retrospectiveImprove,
                retrospectiveNextAction = queryResult.retrospectiveNextAction,
                submittedAt = queryResult.submittedAt,
                lastModifiedAt = queryResult.lastModifiedAt,
            )
        }
    }
}
