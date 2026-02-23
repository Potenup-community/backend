package kr.co.wground.notification.infra.slack

import kr.co.wground.notification.application.port.NotificationMessage
import kr.co.wground.notification.application.port.NotificationSender
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Configuration
@EnableConfigurationProperties(SlackWebhookProperties::class)
class SlackConfig {

    @Bean
    @ConditionalOnProperty(prefix = "slack.webhook", name = ["enabled"], havingValue = "true")
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder
            .connectTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(5))
            .build()
    }

    @Bean
    @ConditionalOnMissingBean(NotificationSender::class)
    fun noopNotificationSender(): NotificationSender =
        object : NotificationSender {
            override fun send(message: NotificationMessage) = Unit
        }
}
