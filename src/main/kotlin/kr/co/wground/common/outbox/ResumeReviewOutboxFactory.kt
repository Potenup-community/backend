package kr.co.wground.common.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import kr.co.wground.common.event.ReviewRequestedEvent
import kr.co.wground.common.outbox.entity.OutboxEventEntity
import org.springframework.stereotype.Component

@Component
class ResumeReviewOutboxFactory(
    private val router: OutboxRouter,
    private val objectMapper: ObjectMapper
) {

    fun requested(
        resumeReviewId: Long,
        hash: String,
        payload: ReviewRequestedEvent
    ): OutboxEventEntity {
        val route = router.resolve(OutboxEventType.RESUME_REVIEW_REQUESTED)
        val payloadJson = objectMapper.writeValueAsString(payload)

        return OutboxEventEntity.of(
            eventType = route.eventType,
            exchange = route.exchange,
            routingKey = route.routingKey,
            payloadJson = payloadJson,
            dedupKey = "rr:req:$hash",
            domainId = resumeReviewId
        )
    }
}
