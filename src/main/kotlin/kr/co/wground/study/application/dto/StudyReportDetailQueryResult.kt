package kr.co.wground.study.application.dto

import kr.co.wground.global.common.UserId
import kr.co.wground.study.domain.StudyReport
import kr.co.wground.study.domain.enums.StudyReportApprovalStatus
import java.time.LocalDateTime

data class StudyReportDetailQueryResult(
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
        fun of(report: StudyReport): StudyReportDetailQueryResult {
            return StudyReportDetailQueryResult(
                reportId = report.id,
                studyId = report.study.id,
                status = report.status,
                week1Activity = report.weeklyActivities.week1Activity,
                week2Activity = report.weeklyActivities.week2Activity,
                week3Activity = report.weeklyActivities.week3Activity,
                week4Activity = report.weeklyActivities.week4Activity,
                retrospectiveGood = report.teamRetrospective.retrospectiveGood,
                retrospectiveImprove = report.teamRetrospective.retrospectiveImprove,
                retrospectiveNextAction = report.teamRetrospective.retrospectiveNextAction,
                submittedAt = report.submittedAt,
                lastModifiedAt = report.lastModifiedAt,
            )
        }
    }
}
