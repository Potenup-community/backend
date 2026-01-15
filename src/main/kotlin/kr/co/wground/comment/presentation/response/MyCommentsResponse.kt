package kr.co.wground.comment.presentation.response

import kr.co.wground.comment.application.dto.CommentSummaryDto
import org.springframework.data.domain.Slice

data class MyCommentsResponse(
    val contents: List<CommentSummaryResponse>,
    val hasNext: Boolean,
    val nextPage: Int?,
)

fun Slice<CommentSummaryDto>.toResponse(): MyCommentsResponse {
    return MyCommentsResponse(
        contents = this.content.map { CommentSummaryResponse.from(it) },
        hasNext = this.hasNext(),
        nextPage = if (this.hasNext()) this.number + 2 else null,
    )
}

