package kr.co.wground.post.application.dto

import kr.co.wground.global.common.WriterId
import kr.co.wground.post.domain.Post
import kr.co.wground.post.domain.enums.HighlightType
import kr.co.wground.post.domain.enums.Topic
import java.util.UUID

data class PostCreateDto(
    val draftId: UUID,
    val writerId: WriterId,
    val topic: Topic,
    val title: String,
    val content: String,
    val highlightType: HighlightType? = null,
) {
    fun toDomain() = Post.from(
        writerId = writerId,
        topic = topic,
        title = title,
        content = content,
        highlightType = highlightType,
    )
}
