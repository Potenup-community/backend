package kr.co.wground.user.application.requestsign

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.auth.GoogleTokenVerifier
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.application.requestsign.event.SignUpEvent
import kr.co.wground.user.application.requestsign.event.DecideUserStatusEvent
import kr.co.wground.user.application.requestsign.event.toReturnUserId
import kr.co.wground.user.application.requestsign.event.toUserEntity
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserSignupStatus
import kr.co.wground.user.infra.RequestSignupRepository
import kr.co.wground.user.infra.UserRepository
import kr.co.wground.user.presentation.request.DecisionStatusRequest
import kr.co.wground.user.presentation.request.SignUpRequest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SignUpServiceImpl(
    private val userRepository: UserRepository,
    private val signupRepository: RequestSignupRepository,
    private val googleTokenVerifier: GoogleTokenVerifier,
    private val eventPublisher: ApplicationEventPublisher
) : SignUpService {

    override fun addUser(request: SignUpRequest) {
        val email = googleTokenVerifier.verify(request.idToken)
        val newUser = request.toUserEntity(email)

        validateExistUser(newUser.email)
        validateUserRole(newUser.role)

        val savedUser = userRepository.save(newUser)

        eventPublisher.publishEvent(SignUpEvent(savedUser.toReturnUserId()))
    }

    override fun decisionSignup(request: DecisionStatusRequest) {
        val requestSign = signupRepository.findByIdOrNull(request.id)
            ?: throw BusinessException(UserServiceErrorCode.REQUEST_SIGNUP_NOT_FOUND)

        validateUserStatus(requestSign.requestStatus)
        requestSign.decide(request.requestStatus)

        val event = DecideUserStatusEvent.from(requestSign.userId, request)
        eventPublisher.publishEvent(event)
    }

    private fun validateExistUser(email: String) {
        if (userRepository.existsUserByEmail(email)) {
            throw BusinessException(UserServiceErrorCode.ALREADY_SIGNED_USER)
        }
    }

    private fun validateUserRole(role: UserRole) {
        if (role == UserRole.ADMIN) {
            throw BusinessException(UserServiceErrorCode.ROLE_ADMIN_CANT_REQUEST)
        }
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
