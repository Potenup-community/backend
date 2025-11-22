package kr.co.wground.user.application.requestsign

import jakarta.transaction.Transactional
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.auth.GoogleTokenVerifier
import kr.co.wground.user.presentation.request.DecisionStatusRequest
import kr.co.wground.user.presentation.request.SignUpRequest
import kr.co.wground.user.domain.RequestSignup
import kr.co.wground.user.domain.User
import kr.co.wground.user.domain.constant.UserSignupStatus
import kr.co.wground.user.domain.constant.UserStatus
import kr.co.wground.user.infra.RequestSignupRepository
import kr.co.wground.user.infra.UserRepository
import kr.co.wground.user.application.exception.UserServiceErrorCode
import org.springframework.stereotype.Service

@Service
class SignUpServiceImpl(
    private val userRepository: UserRepository,
    private val signupRepository: RequestSignupRepository,
    private val googleTokenVerifier: GoogleTokenVerifier
) : SignUpService {

    @Transactional
    override fun addRequestSignUp(requestSignup: SignUpRequest) {
        val email = googleTokenVerifier.verify(requestSignup.idToken)

        checkAlreadyExists(email)

        val newUser = User(
            userId = null,
            email = email,
            name = requestSignup.name,
            phoneNumber = requestSignup.phoneNumber,
            provider = requestSignup.provider,
            affiliationId = requestSignup.affiliationId,
            role = requestSignup.role,
            status = UserStatus.BLOCKED
        ).let { userRepository.save(it) }

        val newRequest = RequestSignup(
            requestSignupId = null,
            email = email,
            affiliationId = requestSignup.affiliationId,
            name = requestSignup.name,
            phoneNumber = requestSignup.phoneNumber,
            provider = requestSignup.provider,
            role = requestSignup.role,
        )
        signupRepository.save(newRequest)
    }

    @Transactional
    override fun decisionSignup(request: DecisionStatusRequest) {
        val requestSignUp = signupRepository.findById(request.userId)
            .orElseThrow { BusinessException(UserServiceErrorCode.REQUEST_SIGNUP_NOT_FOUND) }

        val user = userRepository.findByEmail(requestSignUp.email)
            ?: throw BusinessException(
                UserServiceErrorCode.INVALID_INPUT_VALUE,
            )

        if(request.requestStatus != UserSignupStatus.ACCEPTED){
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