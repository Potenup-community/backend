package kr.co.wground.post.presentation.response

import kr.co.wground.post.application.dto.PostSummaryDto
import kr.co.wground.post.domain.enums.HighlightType
import kr.co.wground.post.domain.enums.Topic
import java.time.LocalDateTime

data class PostSummaryResponse(
    val contents: List<PostSummaryDetail>,
)

data class PostSummaryDetail(
    val postId: Long,
    val title: String,
    val writerId: Long,
    val writerName: String,
    val wroteAt: LocalDateTime,
    val topic: Topic,
    val highlightType: HighlightType?,
    val commentsCount: Int,
)

fun PostSummaryDto.toResponseDetail() = PostSummaryDetail(
    postId = postId,
    title = title,
    writerId = writerId,
    writerName = writerName,
    wroteAt = wroteAt,
    topic = topic,
    highlightType = highlightType,
    commentsCount = commentsCount,
)

fun List<PostSummaryDto>.toResponse() =
    PostSummaryResponse(this.map { it.toResponseDetail() })
