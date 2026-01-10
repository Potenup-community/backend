package kr.co.wground.notification.infra

import kr.co.wground.notification.domain.Notification
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface NotificationJpaRepository: JpaRepository<Notification, Long> {
    fun existsByEventId(eventId: UUID): Boolean
}
