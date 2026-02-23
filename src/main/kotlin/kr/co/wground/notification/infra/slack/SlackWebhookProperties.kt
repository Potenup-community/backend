package kr.co.wground.notification.infra.slack

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "slack.webhook")
data class SlackWebhookProperties(
    val generalUrlBe: String,
    val studyUrlBe: String,
    val generalUrlAi: String,
    val studyUrlAi: String,
    val generalUrlGame: String,
    val studyUrlGame: String,
)
