package kr.co.wground.user.utils.email.event

import kr.co.wground.user.utils.email.application.EmailService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class VerificationEventListener(
    private val emailService: EmailService,
) {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun verificationSendEmail(event: VerificationEvent) {
        event.targets.forEach { target -> emailService.sendMail(target)}
    }
}