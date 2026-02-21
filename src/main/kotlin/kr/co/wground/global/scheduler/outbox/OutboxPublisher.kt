package kr.co.wground.global.scheduler.outbox

import kr.co.wground.common.outbox.entity.OutboxEventEntity
import kr.co.wground.common.outbox.repository.OutboxEventRepository
import kr.co.wground.resumereview.infra.ResumeReviewRepository
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class OutboxPublisher(
    private val outboxEventRepository: OutboxEventRepository,
    private val rabbitTemplate: RabbitTemplate,
    private val resumeReviewRepository: ResumeReviewRepository,
) {
    @Scheduled(fixedDelayString = "\${outbox.publish-interval-ms:1000}")
    fun publishBatch() {
        val now = Instant.now()
        val candidates = outboxEventRepository.findPublishCandidates(now, limit = 50)

        for (event in candidates) {
            try {
                publishOne(event)

                event.markPublished()
                outboxEventRepository.save(event)

                resumeReviewRepository.findByIdOrNull(event.domainId)?.let {
                    resumeReviewRepository.save(it)
                }
            } catch (ex: Exception) {
                event.markFailed(
                    ex.message ?: ex::class.java.name,
                )
                outboxEventRepository.save(event)
            }
        }
    }

    private fun publishOne(event: OutboxEventEntity) {
        rabbitTemplate.convertAndSend(event.exchange, event.routingKey, event.payloadJson) { msg ->
            msg.messageProperties.contentType = "application/json"
            msg.messageProperties.messageId = event.id.toString()
            msg.messageProperties.setHeader("dedupKey", event.dedupKey)
            msg.messageProperties.setHeader("eventType", event.eventType)
            msg
        }
    }
}
