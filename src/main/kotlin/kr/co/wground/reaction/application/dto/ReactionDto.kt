package kr.co.wground.reaction.application.dto

import kr.co.wground.reaction.domain.PostReaction
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.domain.enums.ReactionType

data class ReactionDto(
    val userId: UserId,
    val postId: PostId,
    val action: ReactionType,
) {
    fun toDomain() = PostReaction(userId, postId)
}
