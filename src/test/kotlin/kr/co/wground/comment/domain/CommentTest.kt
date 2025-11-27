package kr.co.wground.comment.domain

import kr.co.wground.comment.exception.CommentErrorCode
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.config.resolver.CurrentUserId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EmptySource

class CommentTest {

    @Test
    @DisplayName("댓글 작성 - 성공")
    fun shouldSuccessWriteComment_whenValidInput() {
        // given
        val writerId = CurrentUserId(1)
        val postId = 1L
        val content = "정상적인 댓글 내용입니다."

        // when
        val comment = Comment.create(writerId, postId, null, content)

        // then
        assertAll(
            { assertThat(comment.writerId).isEqualTo(1) },
            { assertThat(comment.postId).isEqualTo(1) },
            { assertThat(comment.content).isEqualTo("정상적인 댓글 내용입니다.") },
            { assertThat(comment.parentId).isNull() },
        )
    }

    @Test
    @DisplayName("대댓글 생성 - 성공")
    fun shouldSuccessWriteReplyComment_whenValidInput() {
        // given
        val writerId = CurrentUserId(1)
        val postId = 1L
        val parentId = 10L
        val content = "정상적인 대댓글 내용입니다."

        // when
        val comment = Comment.create(writerId, postId, parentId, content)

        // then
        assertThat(comment.parentId).isEqualTo(10)
    }

    @ParameterizedTest
    @EmptySource
    @DisplayName("댓글 생성 - 실패 (내용 공백)")
    fun shouldReturnException_whenContentIsEmpty(content: String) {
        // when & then
        assertThatThrownBy {
            Comment.create(
                CurrentUserId(1),
                postId = 1,
                parentId = null,
                content = content,
            )
        }.isInstanceOf(BusinessException::class.java)
            .hasMessage(CommentErrorCode.CONTENT_IS_EMPTY.message)
    }

    @Test
    @DisplayName("댓글 생성 - 실패 (내용 길이 초과)")
    fun shouldReturnException_whenContentIsTooLong() {
        // given
        val longContent = "a".repeat(2000)

        // when & then
        assertThatThrownBy {
            Comment.create(
                writerId = CurrentUserId(1),
                postId = 1,
                parentId = null,
                content = longContent,
            )
        }.isInstanceOf(BusinessException::class.java)
            .hasMessage(CommentErrorCode.CONTENT_IS_TOO_LONG.message)
    }
}
