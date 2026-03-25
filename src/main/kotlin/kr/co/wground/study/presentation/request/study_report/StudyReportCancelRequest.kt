package kr.co.wground.study.presentation.request.study_report

import jakarta.validation.constraints.Size
import kr.co.wground.study.domain.StudyReportApprovalHistory

data class StudyReportCancelRequest(
    @field:Size(max = StudyReportApprovalHistory.MAX_REASON_LENGTH)
    val reason: String? = null,
)
