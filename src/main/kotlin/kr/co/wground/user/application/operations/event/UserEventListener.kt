package kr.co.wground.user.application.operations.event

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.domain.User
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserSignupStatus
import kr.co.wground.user.infra.UserRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class UserEventListener(
    private val userRepository: UserRepository
) {
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun handle(event: DecideUserStatusEvent) {
        val users = userRepository.findByUserIdIn(event.userId)

        validateUser(users)
        validateIdsSize(event.userId.size, users.size)

        users.forEach { user -> user.decide(event.decision, event.role) }
    }

    private fun validateIdsSize(requestSize: Int, findSize: Int) {
        if (requestSize != findSize) {
            throw BusinessException(UserServiceErrorCode.FIND_IDS_SIZE_DIFFERENT_REQUEST_IDS_SIZE)
        }
    }

    private fun validateUser(users: List<User>) {
        if (users.isEmpty()) {
            throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)
        }
    }
}

data class DecideUserStatusEvent(
    val userId: List<UserId>,
    val decision: UserSignupStatus,
    val role: UserRole?
)
