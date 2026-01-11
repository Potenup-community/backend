package kr.co.wground.comment.presentation.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.wground.comment.application.dto.CommentCreateDto
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.PostId
import kr.co.wground.global.config.resolver.CurrentUserId

@Schema(description = "댓글 생성 요청")
data class CommentCreateRequest(
    @field:Schema(example = "1")
    @field:NotNull(message = "게시글 ID를 입력해주세요.")
    val postId: PostId,

    @field:Schema(example = "null")
    val parentId: CommentId? = null,

    @field:Schema(example = "테스트 댓글 내용")
    @field:NotNull(message = "댓글 내용을 입력해주세요.")
    @field:Size(max = 2000, message = "댓글은 2000자까지 작성할 수 있습니다.")
    val content: String,
) {
    fun toDto(writerId: CurrentUserId) = CommentCreateDto(
        writerId = writerId.value,
        postId = postId,
        parentId = parentId,
        content = content,
    )
}
