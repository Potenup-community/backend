package kr.co.wground.user.service.requestsign

import jakarta.transaction.Transactional
import kr.co.wground.user.domain.RequestSignup
import kr.co.wground.user.domain.User
import kr.co.wground.user.domain.constant.UserStatus
import kr.co.wground.user.repository.RequestSignupRepository
import kr.co.wground.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class SignUpServiceImpl(private val userRepository: UserRepository, private val signupRepository: RequestSignupRepository) : SignUpService {

                                                                                                                                                                                                                                                                                                                                                                @Transactional
    override fun addRequestSignUp(requestSignup: RequestSignup) {
        val user = User(
            id = null,
            affiliationId = requestSignup.affiliationId,
            email = requestSignup.email,
            name = requestSignup.name,
            role = requestSignup.role,
            phoneNumber = requestSignup.phoneNumber,
            provider = requestSignup.provider,
            status = UserStatus.BLOCKED
        )
        userRepository.save(user)
        signupRepository.save(requestSignup)
    }
}