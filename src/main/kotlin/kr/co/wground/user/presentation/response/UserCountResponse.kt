package kr.co.wground.user.presentation.response

import com.fasterxml.jackson.annotation.JsonInclude
import kr.co.wground.user.application.operations.dto.AcademicCount
import kr.co.wground.user.application.operations.dto.RoleCount
import kr.co.wground.user.application.operations.dto.SignupCount
import kr.co.wground.user.application.operations.dto.StatusCount
import kr.co.wground.user.application.operations.dto.UserConditionCountDto

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserCountResponse(
    val totalCount: Long,
    val signupSummary: SignupCount? = null,
    val roleSummary: RoleCount? = null,
    val statusSummary: StatusCount? = null,
    val academicSummary: AcademicCount? = null
){
    companion object {
        fun from(dto : UserConditionCountDto): UserCountResponse{
            return UserCountResponse(
                totalCount = dto.totalCount,
                signupSummary = dto.signupSummary,
                roleSummary = dto.roleSummary,
                statusSummary = dto.statusSummary,
                academicSummary = dto.academicSummary
            )
        }
    }
}