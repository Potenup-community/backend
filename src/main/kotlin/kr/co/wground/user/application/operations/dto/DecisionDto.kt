package kr.co.wground.user.application.operations.dto

import kr.co.wground.global.common.UserId
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserSignupStatus
import kr.co.wground.user.presentation.request.DecisionStatusRequest

data class DecisionDto(
    val userId: UserId,
    val role : UserRole?,
    val requestStatus : UserSignupStatus
){
    companion object{
        fun from(userId: UserId, decisionRequest: DecisionStatusRequest) : DecisionDto{
            return DecisionDto(
                userId = userId,
                role = decisionRequest.role,
                requestStatus = decisionRequest.requestStatus
            )
        }
    }
}
