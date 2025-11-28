package kr.co.wground.comment.presentation.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.wground.comment.application.dto.CommentCreateDto
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.PostId
import kr.co.wground.global.config.resolver.CurrentUserId

data class CommentCreateRequest(
    @field:NotNull(message = "게시글 ID를 입력해주세요.")
    val postId: PostId,
    val parentId: CommentId? = null,
    @field:NotNull(message = "댓글 내용을 입력해주세요.")
    @field:Size(max = 2000, message = "댓글은 2000자까지 작성할 수 있습니다.")
    val content: String,
) {
    fun toDto(writerId: CurrentUserId) = CommentCreateDto(
        writerId = writerId,
        postId = postId,
        parentId = parentId,
        content = content,
    )
}
