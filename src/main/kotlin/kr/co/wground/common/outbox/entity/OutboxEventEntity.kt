package kr.co.wground.common.outbox.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Lob
import jakarta.persistence.Table
import kr.co.wground.global.common.UserId
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "outbox_event",
    indexes = [
        Index(name = "idx_outbox_status_created", columnList = "status, createdAt"),
        Index(name = "idx_outbox_dedup", columnList = "dedupKey", unique = true)
    ]
)
class OutboxEventEntity protected constructor(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(length = 100)
    val eventType: String,

    @Column(length = 200)
    val exchange: String,

    val userId: UserId? = null,

    @Column(length = 200)
    val routingKey: String,

    @Lob
    val payloadJson: String,

    @Column(length = 200, unique = true)
    val dedupKey: String,
    val domainId: Long,

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    var status: OutboxStatus = OutboxStatus.PENDING,
    val createdAt: Instant = Instant.now(),
) {
    var publishedAt: Instant? = null
        protected set
    var lastError: String? = null
        protected set
    var nextRetryAt: Instant? = null
        protected set
    var retryCount: Int = 0
        protected set

    fun markPublished(now: Instant = Instant.now()) {
        status = OutboxStatus.PUBLISHED
        publishedAt = now
        lastError = null
        nextRetryAt = null
    }

    fun markFailed(err: String) {
        status = OutboxStatus.FAILED
        lastError = err.take(1000)
        retryCount += 1
        this.nextRetryAt = computeBackoff(retryCount)
    }

    private fun computeBackoff(retryCount: Int): Instant {
        val seconds = minOf(300, (1 shl minOf(retryCount, 8)))
        return Instant.now().plusSeconds(seconds.toLong())
    }

    companion object {
        fun of(
            domainId: Long,
            eventType: String,
            exchange: String,
            routingKey: String,
            userId: UserId? = null,
            payloadJson: String,
            dedupKey: String,
        ): OutboxEventEntity {
            return OutboxEventEntity(
                userId = userId,
                eventType = eventType,
                exchange = exchange,
                routingKey = routingKey,
                payloadJson = payloadJson,
                dedupKey = dedupKey,
                status = OutboxStatus.PENDING,
                domainId = domainId,
                createdAt = Instant.now(),
            )
        }
    }
}

enum class OutboxStatus { PENDING, PUBLISHED, FAILED }
