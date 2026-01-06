package kr.co.wground.user.application.operations.dto

import kr.co.wground.global.common.UserId
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserSignupStatus
import kr.co.wground.user.presentation.request.DecisionStatusRequest
import kr.co.wground.user.presentation.request.MultipleDecisionRequest

data class DecisionDto(
    val userIds: List<UserId>,
    val role: UserRole?,
    val requestStatus: UserSignupStatus
) {
    companion object {
        fun single(userId: UserId, decisionRequest: DecisionStatusRequest): DecisionDto {
            return DecisionDto(
                userIds = listOf(userId),
                role = decisionRequest.role,
                requestStatus = decisionRequest.requestStatus
            )
        }

        fun from(request: MultipleDecisionRequest): DecisionDto{
            return DecisionDto(
                userIds = request.ids,
                role = request.role,
                requestStatus = request.requestStatus
            )
        }
    }
}
