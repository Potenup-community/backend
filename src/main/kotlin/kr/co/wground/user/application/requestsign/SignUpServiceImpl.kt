package kr.co.wground.user.application.requestsign

import jakarta.transaction.Transactional
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.auth.GoogleTokenVerifier
import kr.co.wground.like.domain.UserId
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.application.requestsign.event.SignUpEvent
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
        val requestSignUp = signupRepository.findByIdOrNull(request.id)
            ?: throw BusinessException(UserServiceErrorCode.REQUEST_SIGNUP_NOT_FOUND)

        validateUserStatus(requestSignUp.requestStatus)

        val user = userRepository.findByIdOrNull(requestSignUp.userId)
            ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

        if (!isAcceptedStatus(requestSignUp.requestStatus)) {
            requestSignUp.reject()
            return
        }

        requestSignUp.approve()
        user.approve()
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

    private fun validateUserStatus(requestSignUp: UserSignupStatus) {
        if (isAcceptedStatus(requestSignUp)) {
            throw BusinessException(UserServiceErrorCode.ALREADY_SIGNED_USER)
        }
    }

    private fun isAcceptedStatus(requestSignUp: UserSignupStatus): Boolean {
        return requestSignUp == UserSignupStatus.ACCEPTED
    }
}
