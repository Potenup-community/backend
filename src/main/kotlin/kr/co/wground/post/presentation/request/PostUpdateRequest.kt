package kr.co.wground.post.presentation.request

import jakarta.validation.constraints.Size
import kr.co.wground.like.domain.PostId
import kr.co.wground.like.domain.UserId
import kr.co.wground.post.application.dto.PostUpdateDto
import kr.co.wground.post.domain.enums.Topic

data class PostUpdateRequest(
    val topic: Topic? = null,
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
