package kr.co.wground.study.application.dto

import kr.co.wground.global.common.UserId
import kr.co.wground.study.domain.StudyReportApprovalHistory
import kr.co.wground.study.domain.enums.StudyReportApprovalAction
import java.time.LocalDateTime

data class StudyReportApprovalHistoryQueryResult(
    val action: StudyReportApprovalAction,
    val actorId: UserId,
    val reason: String?,
    val actedAt: LocalDateTime,
) {
    companion object {
        fun of(history: StudyReportApprovalHistory): StudyReportApprovalHistoryQueryResult {
            return StudyReportApprovalHistoryQueryResult(
                action = history.action,
                actorId = history.actorId,
                reason = history.reason,
                actedAt = history.actedAt,
            )
        }
    }
}
