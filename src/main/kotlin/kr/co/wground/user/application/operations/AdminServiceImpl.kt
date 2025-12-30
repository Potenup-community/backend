package kr.co.wground.user.application.operations

import kr.co.wground.exception.BusinessException
import kr.co.wground.track.infra.TrackRepository
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.application.operations.constant.NOT_ASSOCIATE
import kr.co.wground.user.application.operations.dto.ConditionDto
import kr.co.wground.user.application.operations.event.DecideUserStatusEvent
import kr.co.wground.user.infra.RequestSignupRepository
import kr.co.wground.user.infra.UserRepository
import kr.co.wground.user.infra.dto.UserInfoDto
import kr.co.wground.user.application.operations.dto.AdminSearchUserDto
import kr.co.wground.user.application.operations.dto.DecisionDto
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
    fun decisionSignup(decisionDto: DecisionDto) {
        val requestSign = signupRepository.findByUserId(decisionDto.userId)
            ?: throw BusinessException(UserServiceErrorCode.REQUEST_SIGNUP_NOT_FOUND)

        requestSign.decide(decisionDto.requestStatus)

        val event = DecideUserStatusEvent.from(decisionDto.userId, decisionDto.requestStatus, decisionDto.role)
        eventPublisher.publishEvent(event)
    }

    @Transactional(readOnly = true)
    fun findUsersByConditions(conditionDto: ConditionDto, pageable: Pageable): Page<AdminSearchUserDto> {
        val userInfos = userRepository.searchUsers(conditionDto, pageable)

        validatePageBounds(userInfos, pageable)

        val trackIds = userInfos.content.map { it.trackId }.toSet()
        val tracks = trackRepository.findAllById(trackIds)

        val trackNameMap = tracks.associate { it.trackId to it.trackName }

        return userInfos.map { userInfo ->
            AdminSearchUserDto(
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

    private fun validatePageBounds(userInfos: Page<UserInfoDto>, pageable: Pageable) {
        val requestedPage = pageable.pageNumber
        val totalPages = userInfos.totalPages
        val totalElements = userInfos.totalElements

        validateMinPage(requestedPage)
        validateOverPage(totalPages, totalElements, requestedPage)
        validateElementZeroNextPage(totalElements, requestedPage)
    }

    private fun validateMinPage(requestedPage: Int) {
        if (requestedPage < 0) {
            throw BusinessException(UserServiceErrorCode.PAGE_NUMBER_MIN_ERROR)
        }
    }

    fun validateOverPage(
        totalPages: Int,
        totalElements: Long,
        requestedPage: Int
    ) {
        if (totalElements > 0 && requestedPage >= totalPages) {
            throw BusinessException(UserServiceErrorCode.PAGE_NUMBER_IS_OVER_TOTAL_PAGE)
        }
    }

    private fun validateElementZeroNextPage(totalElements: Long, requestedPage: Int) {
        if (totalElements == 0L && requestedPage > 0) {
            throw BusinessException(UserServiceErrorCode.CANT_REQUEST_NEXT_PAGE_IN_ZERO_ELEMENT)
        }
    }
}
