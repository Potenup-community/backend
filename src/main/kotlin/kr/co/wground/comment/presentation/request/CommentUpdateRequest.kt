package kr.co.wground.comment.presentation.request

import kr.co.wground.comment.application.dto.CommentUpdateDto
import kr.co.wground.global.common.CommentId

data class CommentUpdateRequest(
    val content: String?
) {
    fun toDto(commentId: CommentId) = CommentUpdateDto(
        commentId = commentId,
        content = content
    )
}
