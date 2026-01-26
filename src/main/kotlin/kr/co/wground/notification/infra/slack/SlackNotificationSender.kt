package kr.co.wground.notification.infra.slack

import kr.co.wground.notification.application.port.NotificationMessage
import kr.co.wground.notification.application.port.NotificationSender
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

@Component
class SlackNotificationSender(
    private val restTemplate: RestTemplate,
    private val blockKitBuilder: SlackBlockKitBuilder,
    @param:Value("\${slack.webhook.url}") private val webhookUrl: String,
) : NotificationSender {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun send(message: NotificationMessage) {
        try {
            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

            val payload = blockKitBuilder.build(message)
            val request = HttpEntity(payload, headers)

            restTemplate.postForEntity<String>(webhookUrl, request)
            log.info("Slack message sent successfully: type=${message.type}")
        } catch (e: Exception) {
            log.error("Failed to send Slack message: ${e.message}", e)
        }
    }
}
