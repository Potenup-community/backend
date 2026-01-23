package kr.co.wground.notification.domain

import kr.co.wground.exception.BusinessException
import kr.co.wground.notification.domain.enums.NotificationStatus
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

class NotificationTest {

    private fun createNotification(
        recipientId: Long = 1L,
        actorId: Long? = 2L,
        createdAt: LocalDateTime = LocalDateTime.now(),
        expiresAt: LocalDateTime? = null,
    ): Notification {
        return Notification(
            eventId = UUID.randomUUID(),
            recipientId = recipientId,
            actorId = actorId,
            content = NotificationContent("title", "content"),
            reference = null,
            type = NotificationType.POST_COMMENT,
            status = NotificationStatus.UNREAD,
            createdAt = createdAt,
            expiresAt = expiresAt,
        )
    }

    @Nested
    @DisplayName("알림 생성")
    inner class CreateNotification {

        @Test
        @DisplayName("recipientId가 0이면 BusinessException이 발생한다")
        fun shouldThrowException_whenRecipientIdIsZero() {
            assertThatThrownBy { createNotification(recipientId = 0) }
                .isInstanceOf(BusinessException::class.java)
                .hasMessage(NotificationErrorCode.INVALID_RECIPIENT_ID.message)
        }

        @Test
        @DisplayName("recipientId가 음수이면 BusinessException이 발생한다")
        fun shouldThrowException_whenRecipientIdIsNegative() {
            assertThatThrownBy { createNotification(recipientId = -1) }
                .isInstanceOf(BusinessException::class.java)
                .hasMessage(NotificationErrorCode.INVALID_RECIPIENT_ID.message)
        }

        @Test
        @DisplayName("actorId가 0이면 BusinessException이 발생한다")
        fun shouldThrowException_whenActorIdIsZero() {
            assertThatThrownBy { createNotification(actorId = 0) }
                .isInstanceOf(BusinessException::class.java)
                .hasMessage(NotificationErrorCode.INVALID_ACTOR_ID.message)
        }

        @Test
        @DisplayName("actorId가 음수이면 BusinessException이 발생한다")
        fun shouldThrowException_whenActorIdIsNegative() {
            assertThatThrownBy { createNotification(actorId = -1) }
                .isInstanceOf(BusinessException::class.java)
                .hasMessage(NotificationErrorCode.INVALID_ACTOR_ID.message)
        }

        @Test
        @DisplayName("actorId가 null이면 검증을 통과한다")
        fun shouldPass_whenActorIdIsNull() {
            val notification = createNotification(actorId = null)

            assertThat(notification.actorId).isNull()
        }

        @Test
        @DisplayName("expiresAt이 createdAt 이전이면 BusinessException이 발생한다")
        fun shouldThrowException_whenExpiresAtIsBeforeCreatedAt() {
            val now = LocalDateTime.now()

            assertThatThrownBy {
                createNotification(createdAt = now, expiresAt = now.minusDays(1))
            }
                .isInstanceOf(BusinessException::class.java)
                .hasMessage(NotificationErrorCode.INVALID_EXPIRES_AT.message)
        }

        @Test
        @DisplayName("expiresAt이 createdAt과 같으면 BusinessException이 발생한다")
        fun shouldThrowException_whenExpiresAtEqualsCreatedAt() {
            val now = LocalDateTime.now()

            assertThatThrownBy {
                createNotification(createdAt = now, expiresAt = now)
            }
                .isInstanceOf(BusinessException::class.java)
                .hasMessage(NotificationErrorCode.INVALID_EXPIRES_AT.message)
        }

        @Test
        @DisplayName("expiresAt이 null이면 검증을 통과한다")
        fun shouldPass_whenExpiresAtIsNull() {
            val notification = createNotification(expiresAt = null)

            assertThat(notification.expiresAt).isNull()
        }
    }

    @Nested
    @DisplayName("알림 읽음 처리")
    inner class MarkAsRead {

        @Test
        @DisplayName("읽지 않은 알림을 읽음 처리하면 상태가 READ로 변경된다")
        fun shouldChangeStatusToRead_whenMarkAsReadCalled() {
            val notification = createNotification()

            notification.markAsRead()

            assertThat(notification.status).isEqualTo(NotificationStatus.READ)
        }

        @Test
        @DisplayName("이미 읽은 알림을 다시 읽음 처리해도 상태가 유지된다")
        fun shouldKeepReadStatus_whenAlreadyRead() {
            val notification = createNotification()
            notification.markAsRead()

            notification.markAsRead()

            assertThat(notification.status).isEqualTo(NotificationStatus.READ)
        }
    }

    @Nested
    @DisplayName("알림 만료 확인")
    inner class IsExpired {

        @Test
        @DisplayName("expiresAt이 현재 시간 이전이면 만료되었다")
        fun shouldReturnTrue_whenExpiresAtIsBeforeNow() {
            val now = LocalDateTime.now()
            val notification = createNotification(
                createdAt = now.minusDays(2),
                expiresAt = now.minusDays(1)
            )

            assertThat(notification.isExpired(now)).isTrue()
        }

        @Test
        @DisplayName("expiresAt이 현재 시간 이후이면 만료되지 않았다")
        fun shouldReturnFalse_whenExpiresAtIsAfterNow() {
            val now = LocalDateTime.now()
            val notification = createNotification(
                createdAt = now,
                expiresAt = now.plusDays(1)
            )

            assertThat(notification.isExpired(now)).isFalse()
        }

        @Test
        @DisplayName("expiresAt이 null이면 만료되지 않았다")
        fun shouldReturnFalse_whenExpiresAtIsNull() {
            val notification = createNotification(expiresAt = null)

            assertThat(notification.isExpired(LocalDateTime.now())).isFalse()
        }
    }
}
