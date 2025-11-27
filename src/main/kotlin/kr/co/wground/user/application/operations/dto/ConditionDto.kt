package kr.co.wground.user.application.operations.dto

import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserSignupStatus
import kr.co.wground.user.domain.constant.UserStatus
import kr.co.wground.user.presentation.request.UserSearchRequest

data class ConditionDto(
    val name: String? = null,
    val email: String? = null,
    val trackId: Long? = null,
    val role: UserRole? = null,
    val status: UserStatus? = null,
    val requestStatus: UserSignupStatus? = null,
    val provider : String? = null,
    val isGraduated: Boolean? = null,
) {
    companion object {
        fun from(
            condition: UserSearchRequest
        ): ConditionDto {
            return ConditionDto(
                name = condition.name,
                email = condition.email,
                trackId = condition.trackId,
                role = condition.role,
                status = condition.status,
                requestStatus = condition.requestStatus,
                provider = condition.provider?.uppercase(),
                isGraduated = condition.isGraduated,
            )
        }
    }
}
