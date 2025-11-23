package kr.co.wground.user.application.requestsign.event

import kr.co.wground.exception.BusinessException
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.infra.RequestSignupRepository
import kr.co.wground.user.infra.UserRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionalEventListener
import toRequestSignup
import toUserEntity

@Component
class SignUpEventListener(
    private val userRepository: UserRepository,
    private val signupRepository: RequestSignupRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun saveUser(event: UserAddEvent) {
        val newUser = event.request.toUserEntity(event.email)

        val savedUser = userRepository.save(newUser)

        applicationEventPublisher.publishEvent(SignUpEvent(savedUser))
    }

    @EventListener
    @Transactional
    fun saveSignupLog(event: SignUpEvent) {
        checkAlreadyExists(event.user.userId!!)

        val newRequest = event.user.toRequestSignup()

        signupRepository.save(newRequest)
    }

    private fun checkAlreadyExists(userId: Long) {
        validateExistRequestSign(userId)
        validateExistUser(userId)
    }

    private fun validateExistUser(userId: Long) {
        if (userRepository.existsUserById(userId)) {
            throw BusinessException(UserServiceErrorCode.REQUEST_SIGNUP_ALREADY_EXISTED)
        }
    }

    private fun validateExistRequestSign(userId: Long) {
        if (signupRepository.existsByUserId(userId)) {
            throw BusinessException(UserServiceErrorCode.REQUEST_SIGNUP_ALREADY_EXISTED)
        }
    }
}
