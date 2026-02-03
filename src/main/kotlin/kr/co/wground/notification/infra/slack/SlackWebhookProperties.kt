package kr.co.wground.notification.infra.slack

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "slack.webhook")
data class SlackWebhookProperties(
    val generalUrl: String,
    val studyUrl: String,
)
