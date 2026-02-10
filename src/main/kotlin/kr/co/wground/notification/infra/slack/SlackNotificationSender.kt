package kr.co.wground.notification.infra.slack

import kr.co.wground.notification.application.port.NotificationMessage
import kr.co.wground.notification.application.port.NotificationSender
import org.slf4j.LoggerFactory
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
    private val webhookProperties: SlackWebhookProperties,
) : NotificationSender {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun send(message: NotificationMessage) {
        val webhookUrl = resolveWebhookUrl(message.channel)

        try {
            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

            val payload = blockKitBuilder.build(message)
            val request = HttpEntity(payload, headers)

            restTemplate.postForEntity<String>(webhookUrl, request)
            log.info("Slack message sent successfully: type=${message.type}, channel=${message.channel}")
        } catch (e: Exception) {
            log.error("Failed to send Slack message: channel=${message.channel}, ${e.message}", e)
        }
    }

    private fun resolveWebhookUrl(channel: SlackChannel): String {
        return when (channel) {
            SlackChannel.GENERAL -> webhookProperties.generalUrl
            SlackChannel.STUDY -> webhookProperties.studyUrl
        }
    }
}
