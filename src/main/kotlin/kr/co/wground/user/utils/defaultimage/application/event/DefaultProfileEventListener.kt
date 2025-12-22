package kr.co.wground.user.utils.defaultimage.application.event

import kr.co.wground.user.utils.defaultimage.application.DefaultProfileService
import kr.co.wground.user.utils.defaultimage.application.dto.UserApproveEvent
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class DefaultProfileEventListener(
    private val defaultProfileService: DefaultProfileService
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(
        retryFor = [Exception::class],
        maxAttemptsExpression = "#{avatarProperties.retryMaxAttempts}",
        backoff = Backoff(delayExpression = "#{avatarProperties.retryDelay}")
    )
    fun handleDefaultProfile(event: UserApproveEvent){
        defaultProfileService.createDefaultProfile(
            userId = event.userId,
            email = event.userEmail,
            name = event.userName,
        )
    }
}