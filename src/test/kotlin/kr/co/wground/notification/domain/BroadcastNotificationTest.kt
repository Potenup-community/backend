package kr.co.wground.notification.domain

import kr.co.wground.exception.BusinessException
import kr.co.wground.notification.domain.enums.BroadcastTargetType
import kr.co.wground.notification.domain.enums.NotificationType
import kr.co.wground.notification.domain.vo.NotificationContent
import kr.co.wground.notification.exception.NotificationErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class BroadcastNotificationTest {

    private fun createBroadcastNotification(
        targetType: BroadcastTargetType = BroadcastTargetType.ALL,
        targetId: Long? = null,
        createdAt: LocalDateTime = LocalDateTime.now(),
        expiresAt: LocalDateTime? = null,
    ): BroadcastNotification {
        return BroadcastNotification(
            eventId = UUID.randomUUID(),
            type = NotificationType.ANNOUNCEMENT,
            content = NotificationContent("title", "content"),
            targetType = targetType,
            targetId = targetId,
            reference = null,
            createdAt = createdAt,
            expiresAt = expiresAt,
        )
    }

    @Nested
    @DisplayName("브로드캐스트 알림 생성")
    inner class CreateBroadcastNotification {

        @Test
        @DisplayName("targetType이 ALL이면 targetId가 null이어도 생성된다")
        fun shouldCreate_whenTargetTypeIsAllAndTargetIdIsNull() {
            val notification = createBroadcastNotification(
                targetType = BroadcastTargetType.ALL,
                targetId = null
            )

            assertThat(notification.targetType).isEqualTo(BroadcastTargetType.ALL)
            assertThat(notification.targetId).isNull()
        }

        @Test
        @DisplayName("targetType이 TRACK이고 targetId가 있으면 생성된다")
        fun shouldCreate_whenTargetTypeIsTrackAndTargetIdExists() {
            val notification = createBroadcastNotification(
                targetType = BroadcastTargetType.TRACK,
                targetId = 1L
            )

            assertThat(notification.targetType).isEqualTo(BroadcastTargetType.TRACK)
            assertThat(notification.targetId).isEqualTo(1L)
        }

        @Test
        @DisplayName("targetType이 TRACK인데 targetId가 null이면 예외가 발생한다")
        fun shouldThrowException_whenTargetTypeIsTrackAndTargetIdIsNull() {
            assertThatThrownBy {
                createBroadcastNotification(
                    targetType = BroadcastTargetType.TRACK,
                    targetId = null
                )
            }
                .isInstanceOf(BusinessException::class.java)
                .hasMessage(NotificationErrorCode.INVALID_BROADCAST_TARGET.message)
        }
    }

    @Nested
    @DisplayName("브로드캐스트 알림 만료 확인")
    inner class IsExpired {

        @Test
        @DisplayName("expiresAt이 현재 시간 이전이면 만료되었다")
        fun shouldReturnTrue_whenExpiresAtIsBeforeNow() {
            val now = LocalDateTime.now()
            val notification = createBroadcastNotification(
                createdAt = now.minusDays(2),
                expiresAt = now.minusDays(1)
            )

            assertThat(notification.isExpired(now)).isTrue()
        }

        @Test
        @DisplayName("expiresAt이 현재 시간 이후이면 만료되지 않았다")
        fun shouldReturnFalse_whenExpiresAtIsAfterNow() {
            val now = LocalDateTime.now()
            val notification = createBroadcastNotification(
                createdAt = now,
                expiresAt = now.plusDays(1)
            )

            assertThat(notification.isExpired(now)).isFalse()
        }

        @Test
        @DisplayName("expiresAt이 null이면 만료되지 않았다")
        fun shouldReturnFalse_whenExpiresAtIsNull() {
            val notification = createBroadcastNotification(expiresAt = null)

            assertThat(notification.isExpired(LocalDateTime.now())).isFalse()
        }
    }
}
