package kr.co.wground.user.infra.dto

import kr.co.wground.user.application.operations.dto.AcademicCount
import kr.co.wground.user.application.operations.dto.RoleCount
import kr.co.wground.user.application.operations.dto.SignupCount
import kr.co.wground.user.application.operations.dto.StatusCount

data class UserCountDto(
    val totalCount: Long,
    val signupSummary: SignupCount,
    val roleSummary: RoleCount,
    val statusSummary: StatusCount,
    val academicSummary: AcademicCount
)
