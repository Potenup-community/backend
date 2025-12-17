package kr.co.wground.post.application.dto

import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.WriterId
import kr.co.wground.post.domain.Post
import kr.co.wground.post.domain.enums.HighlightType
import kr.co.wground.post.domain.enums.Topic
import java.time.LocalDateTime

data class PostDetailDto(
    val postId: PostId,
    val writerId: WriterId,
    val writerName: String,
    val title: String,
    val content: String,
    val topic: Topic,
    val highlightType: HighlightType?,
    val commentsCount: Int,
    val wroteAt: LocalDateTime,
)

fun Post.toDto(writerName: String, commentsCount: Int) = PostDetailDto(
    postId = id,
    writerId = writerId,
    writerName = writerName,
    title = postBody.title,
    content = postBody.content,
    topic = topic,
    highlightType = postStatus.highlightType,
    commentsCount = commentsCount,
    wroteAt = createdAt,
)
