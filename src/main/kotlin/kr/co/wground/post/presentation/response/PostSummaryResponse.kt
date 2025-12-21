package kr.co.wground.post.presentation.response

import kr.co.wground.post.application.dto.PostSummaryDto
import kr.co.wground.post.domain.enums.HighlightType
import kr.co.wground.post.domain.enums.Topic
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import java.time.LocalDateTime

data class PostSummaryResponse(
    val contents: List<PostSummaryDetail>,
    val hasNext: Boolean,
    val nextPage: Int?
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

fun Slice<PostSummaryDto>.toResponse(): PostSummaryResponse {
    return PostSummaryResponse(
        contents = this.content.map { it.toResponseDetail() },
        hasNext = this.hasNext(),
        nextPage = if (this.hasNext()) this.number + 1 else null
    )
}
