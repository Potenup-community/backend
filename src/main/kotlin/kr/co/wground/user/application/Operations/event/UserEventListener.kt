package kr.co.wground.user.application.Operations.event

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserSignupStatus
import kr.co.wground.user.infra.UserRepository
import kr.co.wground.user.presentation.request.DecisionStatusRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class UserEventListener(
    private val userRepository: UserRepository
) {
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun handle(event: DecideUserStatusEvent) {
        val user = userRepository.findByIdOrNull(event.userId)
            ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

        user.decide(event.decision,event.role)
    }
}

data class DecideUserStatusEvent(
    val userId: UserId,
    val decision: UserSignupStatus,
    val role: UserRole?
){
    companion object {
        fun from(userId : UserId, status : UserSignupStatus, role: UserRole?): DecideUserStatusEvent{
            return DecideUserStatusEvent(
                userId = userId,
                decision = status,
                role = role
            )
        }
    }
}
