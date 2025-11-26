package kr.co.wground.post.domain.vo

import jakarta.persistence.Embeddable
import jakarta.persistence.Lob
import kr.co.wground.exception.BusinessException
import kr.co.wground.post.exception.PostErrorCode

@Embeddable
data class PostBody(
    val title: String,
    @Lob
    val content: String
) {
    init {
        validateTitle()
        validateContent()
    }

    private fun validateTitle() {
        require(title.length <= 50) {
            throw BusinessException(PostErrorCode.TITLE_TOO_LONG)
        }

        require(title.isNotBlank()) { throw BusinessException(PostErrorCode.TITLE_IS_EMPTY) }
    }

    private fun validateContent() {
        require(content.length <= 5000) { throw BusinessException(PostErrorCode.CONTENT_TOO_LONG) }
    }

    fun updatePostBody(title: String?, content: String?): PostBody {
        return PostBody(title ?: this.title, content ?: this.content)
    }
}