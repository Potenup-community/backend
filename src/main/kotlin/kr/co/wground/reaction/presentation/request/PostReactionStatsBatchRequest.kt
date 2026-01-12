package kr.co.wground.reaction.presentation.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.wground.global.common.PostId

@Schema(description = "여러 게시글에 대한 리액션 정보 요청 바디")
data class PostReactionStatsBatchRequest(
    @field:Schema(example = """{
        "postIds": [1, 2, 3]
    }""")
    @field:NotNull(message = "postId 집합이 비어있습니다.")
    // To Do: 나중에 매직 넘버 상수로 뺄 계획입니다.
    @field:Size(min = 1, max = 50, message = "postId 집합이 너무 작거나 큽니다. 1 이상 50 이하여야 합니다.")
    val postIds: Set<PostId>
)
