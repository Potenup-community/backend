package kr.co.wground.notification.domain.vo

import kr.co.wground.exception.BusinessException
import kr.co.wground.notification.exception.NotificationErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EmptySource
import org.junit.jupiter.params.provider.ValueSource

class NotificationContentTest {

    @ParameterizedTest
    @EmptySource
    @DisplayName("title이 빈 문자열이면 BusinessException이 발생한다")
    fun shouldThrowException_whenTitleIsEmpty(title: String) {
        assertThatThrownBy { NotificationContent(title = title, content = "content") }
            .isInstanceOf(BusinessException::class.java)
            .hasMessage(NotificationErrorCode.INVALID_NOTIFICATION_INPUT.message)
    }

    @ParameterizedTest
    @ValueSource(strings = [" ", "  ", "\t"])
    @DisplayName("title이 공백만 있으면 BusinessException이 발생한다")
    fun shouldThrowException_whenTitleIsBlank(title: String) {
        assertThatThrownBy { NotificationContent(title = title, content = "content") }
            .isInstanceOf(BusinessException::class.java)
            .hasMessage(NotificationErrorCode.INVALID_NOTIFICATION_INPUT.message)
    }

    @ParameterizedTest
    @EmptySource
    @DisplayName("content가 빈 문자열이면 BusinessException이 발생한다")
    fun shouldThrowException_whenContentIsEmpty(content: String) {
        assertThatThrownBy { NotificationContent(title = "title", content = content) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessage(NotificationErrorCode.INVALID_NOTIFICATION_INPUT.message)
    }

    @ParameterizedTest
    @ValueSource(strings = [" ", "  ", "\t"])
    @DisplayName("content가 공백만 있으면 BusinessException이 발생한다")
    fun shouldThrowException_whenContentIsBlank(content: String) {
        assertThatThrownBy { NotificationContent(title = "title", content = content) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessage(NotificationErrorCode.INVALID_NOTIFICATION_INPUT.message)
    }

    @Test
    @DisplayName("title과 content가 유효하면 정상 생성된다")
    fun shouldCreateSuccessfully_whenValidInput() {
        val notificationContent = NotificationContent(title = "title", content = "content")

        assertThat(notificationContent.title).isEqualTo("title")
        assertThat(notificationContent.content).isEqualTo("content")
    }
}
