package kr.co.wground.user.application.common

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.auth.GoogleTokenVerifier
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.application.operations.event.SignUpEvent
import kr.co.wground.user.application.operations.event.toReturnUserId
import kr.co.wground.user.application.operations.event.toUserEntity
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.infra.UserRepository
import kr.co.wground.user.presentation.request.SignUpRequest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SignUpServiceImpl(
    private val userRepository: UserRepository,
    private val googleTokenVerifier: GoogleTokenVerifier,
    private val eventPublisher: ApplicationEventPublisher
) : SignUpService {

    override fun addUser(request: SignUpRequest) {
        val email = googleTokenVerifier.verify(request.idToken)
        val newUser = request.toUserEntity(email)

        validateExistUser(newUser.email)

        val savedUser = userRepository.save(newUser)

        eventPublisher.publishEvent(SignUpEvent(savedUser.toReturnUserId()))
    }

    private fun validateExistUser(email: String) {
        if (userRepository.existsUserByEmail(email)) {
            throw BusinessException(UserServiceErrorCode.ALREADY_SIGNED_USER)
        }
    }
}
