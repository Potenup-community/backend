package kr.co.wground.user.service.requestsign

import jakarta.transaction.Transactional
import kr.co.wground.exception.BusinessException
import kr.co.wground.user.controller.dto.request.SignUpRequest
import kr.co.wground.user.repository.RequestSignupRepository
import kr.co.wground.user.repository.UserRepository
import kr.co.wground.user.service.exception.UserServiceErrorCode
import org.springframework.stereotype.Service

@Service
class SignUpServiceImpl(
    private val userRepository: UserRepository,
    private val signupRepository: RequestSignupRepository
) : SignUpService {

    @Transactional
    override fun addRequestSignUp(requestSignup: SignUpRequest) {
        checkAlreadyExists(requestSignup.email)

        val user = requestSignup.toUser()
        val requestUser = user.toRequestSignUp()

        userRepository.save(user)
        signupRepository.save(requestUser)
    }

    private fun checkAlreadyExists(email: String){
        validateExistUser(email)
        validateExistRequestSign(email)
    }

    private fun validateExistUser(email: String) {
        if(userRepository.existsUserByEmail(email)){
            throw BusinessException(UserServiceErrorCode.REQUEST_SIGNUP_ALREADY_EXISTED)
        }
    }

    private fun validateExistRequestSign(email: String){
        if(signupRepository.existsUserByEmail(email)){
            throw BusinessException(UserServiceErrorCode.REQUEST_SIGNUP_ALREADY_EXISTED)
        }
    }
}