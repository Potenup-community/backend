package kr.co.wground.post.presentation.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import kr.co.wground.post.application.dto.PostUpdateDto
import kr.co.wground.post.domain.enums.HighlightType
import kr.co.wground.post.domain.enums.Topic
import java.util.UUID

@Schema(description = "게시글 수정 요청")
data class PostUpdateRequest(
    @field:Schema(example = "123e4567-e89b-12d3-a456-426655440000")
    @field:NotNull
    val draftId: UUID,
    @field:Schema(example = "NOTICE")
    val topic: Topic? = null,
    @field:Schema(example = "Example Title")
    @field:Size(max = 50, message = "제목은 50자까지 작성할 수 있습니다.")
    val title: String? = null,
    @field:Schema(example = "Example Content")
    @field:Size(max = 5000, message = "본문은 5000자까지 작성할 수 있습니다.")
    val content: String? = null,
    @field:Schema(example = "BY_ADMIN")
    val highlightType: HighlightType? = null,
) {
    fun toDto(id: PostId, writerId: UserId) = PostUpdateDto(
        postId = id,
        draftId = draftId,
        title = title,
        content = content,
        writerId = writerId,
        topic = topic,
        highlightType = highlightType
    )
}
