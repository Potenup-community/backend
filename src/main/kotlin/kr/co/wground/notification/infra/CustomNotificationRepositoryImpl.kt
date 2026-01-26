package kr.co.wground.notification.infra

import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.global.common.RecipientId
import kr.co.wground.notification.domain.QNotification.notification
import kr.co.wground.notification.domain.enums.NotificationStatus

class CustomNotificationRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CustomNotificationRepository {

    override fun markAllAsReadByRecipientId(recipientId: RecipientId): Long {
        return queryFactory
            .update(notification)
            .set(notification.status, NotificationStatus.READ)
            .where(
                notification.recipientId.eq(recipientId),
                notification.status.eq(NotificationStatus.UNREAD)
            )
            .execute()
    }
}
