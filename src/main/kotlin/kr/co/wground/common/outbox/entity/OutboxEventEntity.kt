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
import kotlin.random.Random

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

    fun markFailed(err: String, now: Instant = Instant.now()) {
        lastError = err.take(1000)
        retryCount.plus(1)

        if (retryCount >= 10) {
            status = OutboxStatus.DEAD
            nextRetryAt = null
            return
        }

        status = OutboxStatus.FAILED
        nextRetryAt = computeBackoff(retryCount, now)
    }

    private fun computeBackoff(retryCount: Int, now: Instant): Instant {
        val expiration = 1L shl minOf(retryCount, 8)
        val cap = minOf(256L, expiration)
        val jittered = Random.nextLong(0, cap + 1)
        return now.plusSeconds(jittered)
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

enum class OutboxStatus { PENDING, PUBLISHED, FAILED, DEAD }
