package kr.co.wground.user.application.requestsign

import jakarta.transaction.Transactional
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.auth.GoogleTokenVerifier
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.application.requestsign.event.UserAddEvent
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

    override fun addUser(request: SignUpRequest){
        val email = googleTokenVerifier.verify(request.idToken)

        checkAlreadyExists(email)

        eventPublisher.publishEvent(UserAddEvent(request,email))
    }

    override fun decisionSignup(request: DecisionStatusRequest) {
        val requestSignUp = signupRepository.findByIdOrNull(request.userId)
            ?: throw BusinessException(UserServiceErrorCode.REQUEST_SIGNUP_NOT_FOUND)

        val user = userRepository.findByIdOrNull(request.userId)
            ?: throw BusinessException(
                UserServiceErrorCode.INVALID_INPUT_VALUE,
            )

        if (request.requestStatus != UserSignupStatus.ACCEPTED) {
            requestSignUp.reject()
            return
        }

        requestSignUp.approve()
        user.approve()
    }

    private fun checkAlreadyExists(email: String) {
        validateExistRequestSign(email)
        validateExistUser(email)
    }

    private fun validateExistUser(email: String) {
        if (userRepository.existsUserByEmail(email)) {
            throw BusinessException(UserServiceErrorCode.REQUEST_SIGNUP_ALREADY_EXISTED)
        }
    }

    private fun validateExistRequestSign(email: String) {
        if (signupRepository.existsUserByEmail(email)) {
            throw BusinessException(UserServiceErrorCode.REQUEST_SIGNUP_ALREADY_EXISTED)
        }
    }
}
