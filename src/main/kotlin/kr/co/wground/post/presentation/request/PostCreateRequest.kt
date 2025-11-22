package kr.co.wground.post.presentation.request

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.wground.post.application.dto.PostCreateDto
import kr.co.wground.post.domain.enums.HighlightType
import kr.co.wground.post.domain.enums.Topic

data class PostCreateRequest(
    @field:NotNull(message = "작성할 토픽을 선택해주세요.")
    val topic: Topic,
    @field:NotEmpty(message = "제목을 작성해주세요.")
    @field:Size(max = 50)
    val title: String,
    @field:NotEmpty(message = "본문을 작성해주세요.")
    @field:Size(max = 5000)
    val content: String,
    val highlightType: HighlightType? = null,
) {
    fun toDto(writerId: Long) = PostCreateDto(
        writerId = writerId,
        topic = topic,
        title = title,
        content = content,
        highlightType = highlightType,
    )
}
