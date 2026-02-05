package kr.co.wground.notification.infra.broadcast

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.notification.domain.QBroadcastNotification.broadcastNotification
import kr.co.wground.notification.domain.QBroadcastNotificationRead.broadcastNotificationRead
import kr.co.wground.notification.domain.enums.BroadcastTargetType
import kr.co.wground.notification.domain.repository.BroadcastNotificationWithReadStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl

class CustomBroadcastNotificationRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CustomBroadcastNotificationRepository {

    override fun findByTargetWithReadStatus(
        userId: Long,
        trackId: Long?,
        pageable: Pageable
    ): Slice<BroadcastNotificationWithReadStatus> {
        val results = queryFactory
            .select(broadcastNotification, broadcastNotificationRead.id)
            .from(broadcastNotification)
            .leftJoin(broadcastNotificationRead)
            .on(
                broadcastNotificationRead.notificationId.eq(broadcastNotification.id),
                broadcastNotificationRead.userId.eq(userId)
            )
            .where(targetCondition(trackId))
            .orderBy(broadcastNotification.createdAt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong() + 1)
            .fetch()

        val hasNext = results.size > pageable.pageSize
        val content = results
            .take(pageable.pageSize)
            .map { tuple ->
                BroadcastNotificationWithReadStatus(
                    notification = tuple.get(broadcastNotification)!!,
                    isRead = tuple.get(broadcastNotificationRead.id) != null
                )
            }

        return SliceImpl(content, pageable, hasNext)
    }

    override fun countUnreadByUserIdAndTrackId(userId: Long, trackId: Long?): Long {
        return queryFactory
            .select(broadcastNotification.count())
            .from(broadcastNotification)
            .leftJoin(broadcastNotificationRead)
            .on(
                broadcastNotificationRead.notificationId.eq(broadcastNotification.id),
                broadcastNotificationRead.userId.eq(userId)
            )
            .where(
                targetCondition(trackId),
                broadcastNotificationRead.id.isNull
            )
            .fetchOne() ?: 0L
    }

    private fun targetCondition(trackId: Long?): BooleanExpression {
        val allCondition = broadcastNotification.targetType.eq(BroadcastTargetType.ALL)

        return if (trackId != null) {
            val trackCondition = broadcastNotification.targetType.eq(BroadcastTargetType.TRACK)
                .and(broadcastNotification.targetId.eq(trackId))
            allCondition.or(trackCondition)
        } else {
            allCondition
        }
    }
}
