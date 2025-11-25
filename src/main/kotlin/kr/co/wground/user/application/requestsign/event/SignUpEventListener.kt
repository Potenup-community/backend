package kr.co.wground.user.application.requestsign.event

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.domain.RequestSignup
import kr.co.wground.user.infra.RequestSignupRepository
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SignUpEventListener(
    private val signupRepository: RequestSignupRepository,
) {
    @EventListener
    @Transactional
    fun saveSignupLog(event: SignUpEvent) {
        val userId = event.eventPublishedUserId

        validateExistRequestSign(userId)

        signupRepository.save(RequestSignup(userId))
    }

    private fun validateExistRequestSign(userId: UserId) {
        if (signupRepository.existsByUserId(userId)) {
            throw BusinessException(UserServiceErrorCode.REQUEST_SIGNUP_ALREADY_EXISTED)
        }
    }
}
