package kr.co.wground.notification.domain.vo

import kr.co.wground.exception.BusinessException
import kr.co.wground.notification.domain.enums.ReferenceType
import kr.co.wground.notification.exception.NotificationErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class NotificationReferenceTest {

    @Test
    @DisplayName("referenceId가 0이면 BusinessException이 발생한다")
    fun shouldThrowException_whenReferenceIdIsZero() {
        assertThatThrownBy {
            NotificationReference(referenceType = ReferenceType.POST, referenceId = 0)
        }
            .isInstanceOf(BusinessException::class.java)
            .hasMessage(NotificationErrorCode.INVALID_NOTIFICATION_REFERENCE.message)
    }

    @Test
    @DisplayName("referenceId가 음수이면 BusinessException이 발생한다")
    fun shouldThrowException_whenReferenceIdIsNegative() {
        assertThatThrownBy {
            NotificationReference(referenceType = ReferenceType.POST, referenceId = -1)
        }
            .isInstanceOf(BusinessException::class.java)
            .hasMessage(NotificationErrorCode.INVALID_NOTIFICATION_REFERENCE.message)
    }

    @Test
    @DisplayName("referenceId가 양수이면 정상 생성된다")
    fun shouldCreateSuccessfully_whenReferenceIdIsPositive() {
        val reference = NotificationReference(referenceType = ReferenceType.POST, referenceId = 1)

        assertThat(reference.referenceType).isEqualTo(ReferenceType.POST)
        assertThat(reference.referenceId).isEqualTo(1L)
    }
}

