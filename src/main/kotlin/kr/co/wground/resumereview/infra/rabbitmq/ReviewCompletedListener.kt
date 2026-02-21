package kr.co.wground.resumereview.infra.rabbitmq

import com.fasterxml.jackson.databind.ObjectMapper
import kr.co.wground.common.event.ResumeReviewCompletedEvent
import kr.co.wground.common.outbox.OutboxEventRepository
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.global.config.RabbitMQConfig
import kr.co.wground.resumereview.domain.ResumeReviewStatus
import kr.co.wground.resumereview.exception.ResumeReviewErrorCode
import kr.co.wground.resumereview.infra.ResumeReviewRepository
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Component
@Transactional
class ReviewCompletedListener(
    private val resumeReviewRepository: ResumeReviewRepository,
    private val objectMapper: ObjectMapper,
    private val outboxEventRepository: OutboxEventRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    companion object {
        const val UNKNOWN_ERROR = "unknown"
    }

    @RabbitListener(queues = [RabbitMQConfig.COMPLETED_Q])
    fun onCompleted(event: ReviewCompletedEvent) {
        val resumeReview = resumeReviewRepository.findById(event.resumeReviewId)
            .orElseThrow { BusinessException(ResumeReviewErrorCode.NOT_FOUND_EXCEPTION) }

        if (resumeReview.status.isFinished()) return

        if (event.status.isCompleted()) {
            resumeReview.completed(
                resultJson = objectMapper.writeValueAsString(event.result),
                completedAt = event.completedAt
            )

            outboxEventRepository.findByIdOrNull(event.jobId)?.markPublished()

            val notificationEvent = ResumeReviewCompletedEvent(
                userId = event.userId,
                resumeReviewId = resumeReview.id
            )

            eventPublisher.publishEvent(notificationEvent)
        }

        if (event.status.isFailed()){
            resumeReview.failed(
                errorMessage = event.error ?: UNKNOWN_ERROR,
                completedAt = event.completedAt
            )
        }
    }
}

data class ReviewCompletedEvent(
    val jobId: UUID,
    val resumeReviewId: Long,
    val userId: UserId,
    val status: ResumeReviewStatus,
    val result: Any?,
    val error: String?,
    val completedAt: LocalDateTime,
)
