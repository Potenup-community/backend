package kr.co.wground.notification.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(
    name = "broadcast_notification_read",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_broadcast_read_user_notification",
            columnNames = ["user_id", "notification_id"]
        )
    ]
)
class BroadcastNotificationRead(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "notification_id", nullable = false)
    val notificationId: Long,

    val readAt: LocalDateTime = LocalDateTime.now(),
)
