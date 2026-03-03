package kr.co.wground.common.outbox

import org.springframework.stereotype.Component

@Component
class OutboxRouter {

    fun resolve(type: OutboxEventType): Route =
        when (type) {
            OutboxEventType.RESUME_REVIEW_REQUESTED ->
                Route(
                    eventType = "ResumeReviewRequested",
                    exchange = "resume.review",
                    routingKey = "resume.review.requested"
                )

            OutboxEventType.RESUME_REVIEW_COMPLETED ->
                Route(
                    eventType = "ResumeReviewCompleted",
                    exchange = "resume.review",
                    routingKey = "resume.review.completed"
                )
        }
}

data class Route(
    val eventType: String,
    val exchange: String,
    val routingKey: String
)

enum class OutboxEventType {
    RESUME_REVIEW_REQUESTED,
    RESUME_REVIEW_COMPLETED
}
