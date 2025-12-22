package kr.co.wground.user.utils.defaultimage.application.event

import kr.co.wground.user.utils.defaultimage.application.ProfileService
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class DefaultProfileEventListener(
    private val profileService: ProfileService,
) {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Retryable(
        retryFor = [Exception::class],
        maxAttemptsExpression = "#{avatarProperties.retryMaxAttempts}",
        backoff = Backoff(delayExpression = "#{avatarProperties.retryDelay}")
    )
    fun handleDefaultProfile(event: UserProfileEvent){
        profileService.createDefaultProfile(
            userId = event.userId,
            email = event.userEmail,
            name = event.userName,
        )

    }
}