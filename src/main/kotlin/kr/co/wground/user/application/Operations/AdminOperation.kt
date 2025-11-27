package kr.co.wground.user.application.Operations

import kr.co.wground.exception.BusinessException
import kr.co.wground.user.application.Operations.event.DecideUserStatusEvent
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.domain.constant.UserSignupStatus
import kr.co.wground.user.infra.RequestSignupRepository
import kr.co.wground.user.infra.UserRepository
import kr.co.wground.user.presentation.request.DecisionStatusRequest
import kr.co.wground.user.presentation.request.UserSearchRequest
import kr.co.wground.user.presentation.response.UserListResponse
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class AdminOperation(
    val signupRepository: RequestSignupRepository,
    val userRepository: UserRepository,
    private val eventPublisher: ApplicationEventPublisher
): UserOperations {
    fun decisionSignup(request: DecisionStatusRequest) {
        val requestSign = signupRepository.findByIdOrNull(request.id)
            ?: throw BusinessException(UserServiceErrorCode.REQUEST_SIGNUP_NOT_FOUND)

        validateUserStatus(requestSign.requestStatus)
        requestSign.decide(request.requestStatus)

        val event = DecideUserStatusEvent.from(requestSign.userId, request.requestStatus,request.role)
        eventPublisher.publishEvent(event)
    }

    fun findUsersByConditions(conditions : UserSearchRequest, pageable: Pageable): Page<UserListResponse> {
        val users = userRepository.searchUsers(conditions, pageable)
        return users.map{user -> UserListResponse(
            userId = user.userId,
            name = user.name,
            email = user.email,
            phoneNumber = user.phoneNumber,
            trackId = user.trackId,
            role = user.role,
            status = user.status,
            requestStatus = user.requestStatus,
            createdAt = user.createdAt
        ) }
    }

    private fun validateUserStatus(requestStatus: UserSignupStatus) {
        if (isAcceptedStatus(requestStatus)) {
            throw BusinessException(UserServiceErrorCode.ALREADY_SIGNED_USER)
        }
    }

    private fun isAcceptedStatus(requestSignUp: UserSignupStatus): Boolean {
        return requestSignUp == UserSignupStatus.ACCEPTED
    }
}