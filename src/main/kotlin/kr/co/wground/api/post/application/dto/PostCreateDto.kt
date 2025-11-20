package kr.co.wground.api.post.application.dto

import kr.co.wground.api.post.domain.HighlightType
import kr.co.wground.api.post.domain.Post
import kr.co.wground.api.post.domain.Topic

data class PostCreateDto(
    val writerId: Long,
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