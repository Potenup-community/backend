package kr.co.wground.post.presentation.request

import jakarta.validation.constraints.Size
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import kr.co.wground.post.application.dto.PostUpdateDto

data class PostUpdateRequest(
    @field:Size(max = 50, message = "제목은 50자까지 작성할 수 있습니다.")
    val title: String? = null,
    @field:Size(max = 5000, message = "본문은 5000자까지 작성할 수 있습니다.")
    val content: String? = null,
) {
    fun toDto(id: PostId, writerId: UserId) = PostUpdateDto(
        id = id,
        title = title,
        content = content,
        writerId = writerId
    )
}
