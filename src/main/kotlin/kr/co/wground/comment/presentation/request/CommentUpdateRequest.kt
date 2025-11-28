package kr.co.wground.comment.presentation.request

import jakarta.validation.constraints.Size
import kr.co.wground.comment.application.dto.CommentUpdateDto
import kr.co.wground.global.common.CommentId

data class CommentUpdateRequest(
    @field:Size(max = 2000, message = "댓글은 2000자까지 작성할 수 있습니다.")
    val content: String?
) {
    fun toDto(commentId: CommentId) = CommentUpdateDto(
        commentId = commentId,
        content = content
    )
}
