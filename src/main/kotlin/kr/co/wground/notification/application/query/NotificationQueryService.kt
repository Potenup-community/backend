package kr.co.wground.notification.application.query

import kr.co.wground.global.common.RecipientId
import kr.co.wground.notification.application.query.dto.NotificationSummaryDto
import kr.co.wground.notification.domain.repository.NotificationRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class NotificationQueryService(
    private val notificationRepository: NotificationRepository,
) {

    fun getNotifications(recipientId: RecipientId, pageable: Pageable): Slice<NotificationSummaryDto> {
        return notificationRepository.findByRecipientId(recipientId, pageable)
            .map { NotificationSummaryDto.from(it) }
    }

    fun getUnreadCount(recipientId: RecipientId): Long {
        return notificationRepository.countUnreadByRecipientId(recipientId)
    }
}
