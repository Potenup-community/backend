package kr.co.wground.comment.domain

import kr.co.wground.comment.exception.CommentErrorCode
import kr.co.wground.exception.BusinessException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EmptySource

class CommentTest {

    private fun shouldThrowContentEmptyException(action: () -> Unit) {
        assertThatThrownBy { action() }
            .isInstanceOf(BusinessException::class.java)
            .hasMessage(CommentErrorCode.CONTENT_IS_EMPTY.message)
    }

    private fun shouldThrowContentTooLongException(action: () -> Unit) {
        assertThatThrownBy { action() }
            .isInstanceOf(BusinessException::class.java)
            .hasMessage(CommentErrorCode.CONTENT_IS_TOO_LONG.message)
    }

    private fun createValidComment(): Comment =
        Comment.create(
            writerId = 1L,
            postId = 1L,
            parentId = null,
            content = "원래 내용",
        )

    @Nested
    @DisplayName("댓글 생성")
    inner class CreateComment {

        @Test
        fun shouldSuccessWriteComment_whenValidInput() {
            // given
            val writerId = 1L
            val postId = 1L
            val content = "정상적인 댓글 내용입니다."

            // when
            val comment = Comment.create(writerId, postId, null, content)

            // then
            assertAll(
                { assertThat(comment.writerId).isEqualTo(1L) },
                { assertThat(comment.postId).isEqualTo(1L) },
                { assertThat(comment.content).isEqualTo("정상적인 댓글 내용입니다.") },
                { assertThat(comment.parentId).isNull() },
            )
        }

        @Test
        fun shouldSuccessWriteReplyComment_whenValidInput() {
            // given
            val writerId = 1L
            val postId = 1L
            val parentId = 10L
            val content = "정상적인 대댓글 내용입니다."

            // when
            val comment = Comment.create(writerId, postId, parentId, content)

            // then
            assertThat(comment.parentId).isEqualTo(10L)
        }

        @ParameterizedTest
        @EmptySource
        fun shouldThrowException_whenContentIsEmptyOnCreate(content: String) {
            // when & then
            shouldThrowContentEmptyException {
                Comment.create(
                    writerId = 1L,
                    postId = 1L,
                    parentId = null,
                    content = content,
                )
            }
        }

        @Test
        fun shouldThrowException_whenContentIsTooLongOnCreate() {
            // given
            val longContent = "a".repeat(2001)

            // when & then
            shouldThrowContentTooLongException {
                Comment.create(
                    writerId = 1L,
                    postId = 1L,
                    parentId = null,
                    content = longContent,
                )
            }
        }
    }

    @Nested
    @DisplayName("댓글 수정")
    inner class UpdateComment {

        @Test
        fun shouldUpdateContentAndModifiedAt_whenContentChanged() {
            // given
            val comment = createValidComment()
            val beforeModifiedAt = comment.modifiedAt

            // when
            comment.updateContent("수정된 내용")

            // then
            assertAll(
                { assertThat(comment.content).isEqualTo("수정된 내용") },
                { assertThat(comment.modifiedAt).isAfterOrEqualTo(beforeModifiedAt) },
            )
        }

        @Test
        fun shouldNotUpdateModifiedAt_whenContentIsSame() {
            // given
            val originalContent = "원래 내용"
            val comment = Comment.create(
                writerId = 1L,
                postId = 1L,
                parentId = null,
                content = originalContent,
            )
            val beforeModifiedAt = comment.modifiedAt

            // when
            comment.updateContent(originalContent)

            // then
            assertAll(
                { assertThat(comment.content).isEqualTo(originalContent) },
                { assertThat(comment.modifiedAt).isEqualTo(beforeModifiedAt) },
            )
        }

        @ParameterizedTest
        @EmptySource
        fun shouldThrowException_whenContentIsEmptyOnUpdate(newContent: String) {
            // given
            val comment = createValidComment()

            // when & then
            shouldThrowContentEmptyException {
                comment.updateContent(newContent)
            }
        }

        @Test
        fun shouldThrowException_whenContentIsTooLongOnUpdate() {
            // given
            val comment = createValidComment()
            val longContent = "a".repeat(2001)

            // when & then
            shouldThrowContentTooLongException {
                comment.updateContent(longContent)
            }
        }
    }
}
