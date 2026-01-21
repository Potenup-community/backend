package kr.co.wground.comment.presentation.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size
import kr.co.wground.comment.application.dto.CommentUpdateDto
import kr.co.wground.global.common.CommentId

@Schema(description = "댓글 수정 요청")
data class CommentUpdateRequest(
    @field:Schema(example = "수정할 댓글 내용")
    @field:Size(max = 2000, message = "댓글은 2000자까지 작성할 수 있습니다.")
    val content: String?,
    val mentionUserIds: List<Long>? = emptyList(),
) {
    fun toDto(commentId: CommentId) = CommentUpdateDto(
        commentId = commentId,
        content = content
    )
}
