package kr.co.wground.user.utils.email.application

import kr.co.wground.user.utils.email.event.VerificationEvent.VerificationTarget


interface EmailService {
    fun sendMail(event: VerificationTarget)
}