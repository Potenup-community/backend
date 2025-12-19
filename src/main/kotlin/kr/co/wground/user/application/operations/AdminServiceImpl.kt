package kr.co.wground.user.application.operations

import kr.co.wground.exception.BusinessException
import kr.co.wground.track.infra.TrackRepository
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.application.operations.constant.NOT_ASSOCIATE
import kr.co.wground.user.application.operations.dto.ConditionDto
import kr.co.wground.user.application.operations.event.DecideUserStatusEvent
import kr.co.wground.user.infra.RequestSignupRepository
import kr.co.wground.user.infra.UserRepository
import kr.co.wground.user.presentation.request.DecisionStatusRequest
import kr.co.wground.user.presentation.request.UserSearchRequest
import kr.co.wground.user.presentation.response.AdminSearchUserResponse
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
    val trackRepository: TrackRepository,
    private val eventPublisher: ApplicationEventPublisher
) : UserOperations {
    fun decisionSignup(request: DecisionStatusRequest) {
        val requestSign = signupRepository.findByIdOrNull(request.id)
            ?: throw BusinessException(UserServiceErrorCode.REQUEST_SIGNUP_NOT_FOUND)

        requestSign.decide(request.requestStatus)

        val event = DecideUserStatusEvent.from(requestSign.userId, request.requestStatus, request.role)
        eventPublisher.publishEvent(event)
    }

    @Transactional(readOnly = true)
    fun findUsersByConditions(conditions: UserSearchRequest, pageable: Pageable): Page<AdminSearchUserResponse> {
        val conditionDto = ConditionDto.from(conditions)
        val userInfos = userRepository.searchUsers(conditionDto, pageable)

        val trackIds = userInfos.map { it.trackId }.toSet()
        val tracks = trackRepository.findAllById(trackIds)

        val trackNameMap = tracks.associate { it.trackId to it.trackName }

        return userInfos.map { userInfo ->
            AdminSearchUserResponse(
                userId = userInfo.userId,
                name = userInfo.name,
                email = userInfo.email,
                phoneNumber = userInfo.phoneNumber,
                trackName = (trackNameMap[userInfo.trackId] ?: NOT_ASSOCIATE),
                role = userInfo.role,
                status = userInfo.status,
                provider = userInfo.provider,
                requestStatus = userInfo.requestStatus,
                createdAt = userInfo.createdAt
            )
        }
    }
}
