package kr.co.wground.reaction.presentation.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.wground.global.common.PostId

data class PostReactionStatsBatchRequest(
    @field:NotNull(message = "postId 집합이 비어있습니다.")
    // To Do: 나중에 매직 넘버 상수로 뺄 계획입니다.
    @field:Size(min = 1, max = 50, message = "postId 집합이 너무 작거나 큽니다. 1 이상 50 이하여야 합니다.")
    val postIds: Set<PostId>
)
