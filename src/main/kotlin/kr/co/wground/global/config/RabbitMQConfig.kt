package kr.co.wground.global.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig {
    companion object {
        const val EXCHANGE = "resume.review"
        const val DLX = "resume.review.dlx"

        const val REQUESTED_Q = "resume.review.requested.q"
        const val COMPLETED_Q = "resume.review.completed.q"

        const val REQUESTED_DLQ = "resume.review.requested.dlq"
        const val COMPLETED_DLQ = "resume.review.completed.dlq"

        const val ROUTING_KEY_REQUESTED = "review.requested"
        const val ROUTING_KEY_COMPLETED = "review.completed"

        const val ROUTING_KEY_REQUESTED_DLQ = "review.requested.dlq"
        const val ROUTING_KEY_COMPLETED_DLQ = "review.completed.dlq"
    }

    @Bean
    fun exchange() = TopicExchange(EXCHANGE)

    @Bean
    fun dlx() = TopicExchange(DLX)

    @Bean
    fun requestedQueue(): Queue =
        QueueBuilder.durable(REQUESTED_Q)
            .deadLetterExchange(DLX)
            .deadLetterRoutingKey(ROUTING_KEY_REQUESTED_DLQ)
            .build()

    @Bean
    fun completedQueue(): Queue =
        QueueBuilder.durable(COMPLETED_Q)
            .deadLetterExchange(DLX)
            .deadLetterRoutingKey(ROUTING_KEY_COMPLETED_DLQ)
            .build()

    @Bean fun requestedDlq(): Queue = QueueBuilder.durable(REQUESTED_DLQ).build()
    @Bean fun completedDlq(): Queue = QueueBuilder.durable(COMPLETED_DLQ).build()

    @Bean
    fun requestedBinding(exchange: TopicExchange, requestedQueue: Queue) =
        BindingBuilder.bind(requestedQueue).to(exchange).with(ROUTING_KEY_REQUESTED)

    @Bean
    fun completedBinding(exchange: TopicExchange, completedQueue: Queue) =
        BindingBuilder.bind(completedQueue).to(exchange).with(ROUTING_KEY_COMPLETED)

    @Bean
    fun requestedDlqBinding(dlx: TopicExchange, requestedDlq: Queue) =
        BindingBuilder.bind(requestedDlq).to(dlx).with(ROUTING_KEY_REQUESTED_DLQ)

    @Bean
    fun completedDlqBinding(dlx: TopicExchange, completedDlq: Queue) =
        BindingBuilder.bind(completedDlq).to(dlx).with(ROUTING_KEY_COMPLETED_DLQ)

    @Bean
    fun messageConverter(objectMapper: ObjectMapper) =
        Jackson2JsonMessageConverter(objectMapper)

    @Bean
    fun rabbitTemplate(cf: ConnectionFactory, converter: MessageConverter): RabbitTemplate =
        RabbitTemplate(cf).apply { messageConverter = converter }
}
