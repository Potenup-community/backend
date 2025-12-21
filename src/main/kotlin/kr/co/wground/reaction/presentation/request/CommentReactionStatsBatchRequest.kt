package kr.co.wground.reaction.presentation.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.wground.global.common.CommentId

data class CommentReactionStatsBatchRequest(
    @field:NotNull(message = "빈 commentId 집합")
    // To Do: 나중에 매직 넘버 상수로 뺄 계획입니다.
    @field:Size(min = 1, max = 50, message = "commentId 집합이 너무 작거나 큽니다. 1 이상 50 이하여야 합니다.")
    val commentIds: Set<CommentId>
)
