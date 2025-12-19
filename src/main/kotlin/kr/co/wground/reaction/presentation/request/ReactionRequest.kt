package kr.co.wground.reaction.presentation.request

import kr.co.wground.reaction.application.dto.ReactionDto
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.domain.enums.ReactionType

// To Do: 수연님이랑 논의했던 내용 바탕으로 수정해야 함(reactionTarget 추가, ...)
data class ReactionRequest(
    val postId: PostId,
    val reactionType: ReactionType,
) {
    fun toDto(userId: UserId) = ReactionDto(
        userId = userId,
        postId = postId,
        reactionType = reactionType,
    )
}
