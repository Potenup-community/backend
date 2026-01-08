package kr.co.wground.user.utils.email.application

import kr.co.wground.user.utils.email.application.constant.EmailConstants
import kr.co.wground.user.utils.email.event.VerificationEvent.VerificationTarget
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.time.format.DateTimeFormatter

@Component
class EmailServiceImpl(
    private val javaMailSender: JavaMailSender,
    private val templateEngine: TemplateEngine,
    @Value("\${spring.mail.username}")
    private val sendUser: String,
):EmailService {
    override fun sendMail(event: VerificationTarget) {
        val message = javaMailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, EmailConstants.ENCODING)

        val context = Context().apply {
            setVariable(EmailConstants.USER_NAME, event.username)
            setVariable(EmailConstants.TRACK_NAME, event.trackName)
            setVariable(EmailConstants.APPROVED_AT, event.approveAt.format(DateTimeFormatter.ofPattern(EmailConstants.DATE_PATTERN)))
        }

        val htmlContent = templateEngine.process(EmailConstants.TEMPLATE, context)

        helper.setFrom(sendUser)
        helper.setTo(event.email)
        helper.setSubject(EmailConstants.SUBJECT)
        helper.setText(htmlContent, true)

        javaMailSender.send(message)
    }
}