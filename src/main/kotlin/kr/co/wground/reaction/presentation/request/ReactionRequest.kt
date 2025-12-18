package kr.co.wground.reaction.presentation.request

import kr.co.wground.reaction.application.dto.ReactionDto
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.domain.enums.ReactionAction

data class ReactionRequest(
    val postId: PostId,
    val action: ReactionAction,
) {
    fun toDto(userId: UserId) = ReactionDto(
        userId = userId,
        postId = postId,
        action = action,
    )
}
