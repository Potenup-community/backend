package kr.co.wground.post.presentation.request

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.wground.post.application.dto.PostCreateDto
import kr.co.wground.post.domain.enums.HighlightType
import kr.co.wground.post.domain.enums.Topic
import java.util.UUID

data class PostCreateRequest(
    @field:NotNull(message = "드래프트 아이디를 넣어주세요.")
    val draftId: UUID,
    @field:NotNull(message = "작성할 토픽을 선택해주세요.")
    val topic: Topic,
    @field:NotEmpty(message = "제목을 작성해주세요.")
    @field:Size(max = 50, message = "제목은 50자까지 작성할 수 있습니다.")
    val title: String,
    @field:NotEmpty(message = "본문을 작성해주세요.")
    @field:Size(max = 5000, message = "본문은 5000자까지 작성할 수 있습니다.")
    val content: String,
    val highlightType: HighlightType? = null,
) {
    fun toDto(writerId: Long) = PostCreateDto(
        draftId = draftId,
        writerId = writerId,
        topic = topic,
        title = title,
        content = content,
        highlightType = highlightType,
    )
}
