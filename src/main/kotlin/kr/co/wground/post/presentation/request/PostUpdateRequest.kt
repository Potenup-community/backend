package kr.co.wground.post.presentation.request

import jakarta.validation.constraints.Size
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import kr.co.wground.post.application.dto.PostUpdateDto
import kr.co.wground.post.domain.enums.HighlightType
import kr.co.wground.post.domain.enums.Topic
import java.util.UUID

data class PostUpdateRequest(
    val topic: Topic? = null,
    @field:Size(max = 50, message = "제목은 50자까지 작성할 수 있습니다.")
    val title: String? = null,
    @field:Size(max = 5000, message = "본문은 5000자까지 작성할 수 있습니다.")
    val content: String? = null,
    val highlightType: HighlightType? = null,
) {
    fun toDto(id: PostId, writerId: UserId) = PostUpdateDto(
        postId = id,
        title = title,
        content = content,
        writerId = writerId,
        topic = topic,
        highlightType = highlightType
    )
}
