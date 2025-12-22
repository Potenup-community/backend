package kr.co.wground.comment.presentation.response

import kr.co.wground.comment.application.dto.CommentSummaryDto
import org.springframework.data.domain.Slice

data class CommentSliceResponse(
    val contents: List<CommentSummaryResponse>,
    val hasNext: Boolean,
    val nextPage: Int?,
)

fun Slice<CommentSummaryDto>.toResponse(): CommentSliceResponse {
    val mapped = this.content.map(CommentSummaryResponse::from)
    return CommentSliceResponse(
        contents = mapped,
        hasNext = this.hasNext(),
        nextPage = if (this.hasNext()) this.number + 1 else null
    )
}
