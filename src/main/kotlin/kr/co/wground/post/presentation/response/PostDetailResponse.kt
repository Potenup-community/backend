package kr.co.wground.post.presentation.response

import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.WriterId
import kr.co.wground.post.application.dto.PostDetailDto
import kr.co.wground.post.domain.enums.HighlightType
import kr.co.wground.post.domain.enums.Topic
import java.time.LocalDateTime

data class PostDetailResponse(
    val postId: PostId,
    val writerId: WriterId,
    val writerName: String,
    val title: String,
    val content: String,
    val topic: Topic,
    val highlightType: HighlightType?,
    val commentsCount: Int,
    val wroteAt: LocalDateTime,
    val likeCount: Int,
    val reactionCount: Int,
)

fun PostDetailDto.toResponse() = PostDetailResponse(
    postId = postId,
    writerId = writerId,
    writerName = writerName,
    title = title,
    content = content,
    topic = topic,
    highlightType = highlightType,
    commentsCount = commentsCount,
    wroteAt = wroteAt,
    likeCount = likeCount,
    reactionCount = reactionCount,
)
