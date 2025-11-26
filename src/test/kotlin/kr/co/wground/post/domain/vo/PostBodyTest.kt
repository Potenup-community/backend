package kr.co.wground.post.domain.vo

import kr.co.wground.exception.BusinessException
import kr.co.wground.post.exception.PostErrorCode
import org.assertj.core.api.Assertions
import kotlin.test.Test

class PostBodyTest {

    @Test
    fun shouldException_WhenTitleOverMaximumLength() {
        val title = "a".repeat(51)

        Assertions.assertThatThrownBy { PostBody(title, "") }
            .isInstanceOf(BusinessException::class.java)
            .hasMessage(PostErrorCode.TITLE_TOO_LONG.message)
    }

    @Test
    fun shouldException_WhenContentOverMaximumLength() {
        val content = "a".repeat(5001)

        Assertions.assertThatThrownBy { PostBody("title", content) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessage(PostErrorCode.CONTENT_TOO_LONG.message)
    }

    @Test
    fun shouldException_WhenTitleEmpty() {
        val title = ""

        Assertions.assertThatThrownBy { PostBody(title, "content") }
            .isInstanceOf(BusinessException::class.java)
            .hasMessage(PostErrorCode.TITLE_IS_EMPTY.message)
    }
}