package kr.co.wground.user.application.operations

import kr.co.wground.exception.BusinessException
import kr.co.wground.user.application.operations.event.DecideUserStatusEvent
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.application.operations.dto.ConditionDto
import kr.co.wground.user.infra.RequestSignupRepository
import kr.co.wground.user.infra.UserRepository
import kr.co.wground.user.infra.dto.UserInfoDto
import kr.co.wground.user.presentation.request.DecisionStatusRequest
import kr.co.wground.user.presentation.request.UserSearchRequest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AdminServiceImpl(
    val signupRepository: RequestSignupRepository,
    val userRepository: UserRepository,
    private val eventPublisher: ApplicationEventPublisher
): UserOperations {
    fun decisionSignup(request: DecisionStatusRequest) {
        val requestSign = signupRepository.findByIdOrNull(request.id)
            ?: throw BusinessException(UserServiceErrorCode.REQUEST_SIGNUP_NOT_FOUND)

        requestSign.decide(request.requestStatus)

        val event = DecideUserStatusEvent.from(requestSign.userId, request.requestStatus,request.role)
        eventPublisher.publishEvent(event)
    }
    @Transactional(readOnly = true)
    fun findUsersByConditions(conditions : UserSearchRequest, pageable: Pageable): Page<UserInfoDto> {
        val conditionDto = ConditionDto.from(conditions)
        return userRepository.searchUsers(conditionDto, pageable)
    }
}
