package kr.co.wground.study.presentation.response.study_report

import kr.co.wground.global.common.UserId
import kr.co.wground.study.application.dto.StudyReportApprovalHistoryQueryResult
import kr.co.wground.study.domain.enums.StudyReportApprovalAction
import java.time.LocalDateTime

data class StudyReportApprovalHistoryResponse(
    val action: StudyReportApprovalAction,
    val actorId: UserId,
    val reason: String?,
    val timestamp: LocalDateTime,
) {
    companion object {
        fun from(queryResult: StudyReportApprovalHistoryQueryResult): StudyReportApprovalHistoryResponse {
            return StudyReportApprovalHistoryResponse(
                action = queryResult.action,
                actorId = queryResult.actorId,
                reason = queryResult.reason,
                timestamp = queryResult.timestamp,
            )
        }
    }
}
