package kr.co.wground.notification.application.query

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.RecipientId
import kr.co.wground.notification.application.query.dto.NotificationSummaryDto
import kr.co.wground.notification.domain.repository.BroadcastNotificationRepository
import kr.co.wground.notification.domain.repository.NotificationRepository
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.infra.UserRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class NotificationQueryService(
    private val notificationRepository: NotificationRepository,
    private val broadcastNotificationRepository: BroadcastNotificationRepository,
    private val userRepository: UserRepository,
) {

    fun getNotifications(recipientId: RecipientId, pageable: Pageable): Slice<NotificationSummaryDto> {
        val user = userRepository.findByIdOrNull(recipientId)
            ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

        // 개인 알림 조회
        val personalNotifications = notificationRepository
            .findByRecipientId(recipientId, pageable)
            .map { NotificationSummaryDto.fromPersonal(it) }
            .content

        // 브로드캐스트 알림 조회
        val broadcastNotifications = broadcastNotificationRepository
            .findByTargetWithReadStatus(recipientId, user.trackId, pageable)
            .map { NotificationSummaryDto.fromBroadcast(it) }
            .content

        // 합쳐서 최신순 정렬
        val merged = (personalNotifications + broadcastNotifications)
            .sortedByDescending { it.createdAt }
            .take(pageable.pageSize)

        // hasNext 판단: 두 소스 중 하나라도 더 있으면 true
        val hasNext = personalNotifications.size == pageable.pageSize ||
                broadcastNotifications.size == pageable.pageSize

        return SliceImpl(merged, pageable, hasNext)
    }

    fun getUnreadCount(recipientId: RecipientId): Long {
        val user = userRepository.findByIdOrNull(recipientId)
            ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

        val personalUnread = notificationRepository.countUnreadByRecipientId(recipientId)
        val broadcastUnread = broadcastNotificationRepository
            .countUnreadByUserIdAndTrackId(recipientId, user.trackId)

        return personalUnread + broadcastUnread
    }
}
