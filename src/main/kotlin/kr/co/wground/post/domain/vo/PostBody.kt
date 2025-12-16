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
    companion object {
        const val MAX_TITLE_LENGTH = 50
        const val MAX_CONTENT_LENGTH = 5000
    }

    init {
        validateTitle()
        validateContent()
    }

    private fun validateTitle() {
        require(title.length <= MAX_TITLE_LENGTH) {
            throw BusinessException(PostErrorCode.TITLE_TOO_LONG)
        }

        require(title.isNotBlank()) { throw BusinessException(PostErrorCode.TITLE_IS_EMPTY) }
    }

    private fun validateContent() {
        require(content.length <= MAX_CONTENT_LENGTH) {
            throw BusinessException(PostErrorCode.CONTENT_TOO_LONG)
        }
    }

    fun updatePostBody(title: String?, content: String?): PostBody {
        return PostBody(title ?: this.title, content ?: this.content)
    }
}
