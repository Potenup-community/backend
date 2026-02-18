package kr.co.wground.study.presentation.request.study_report

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kr.co.wground.study.domain.StudyReportApprovalHistory

data class StudyReportRejectRequest(
    @field:NotBlank(message = "반려 사유는 필수 입력입니다.")
    @field:Size(max = StudyReportApprovalHistory.MAX_REASON_LENGTH)
    val reason: String,
)
