package kr.co.wground.study.application.dto

import kr.co.wground.study.domain.enums.StudyReportApprovalStatus

data class StudyReportSearchCondition(
    val status: StudyReportApprovalStatus? = null,
)
