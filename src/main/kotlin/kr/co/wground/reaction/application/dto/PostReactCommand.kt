package kr.co.wground.reaction.application.dto

import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.domain.PostReaction
import kr.co.wground.reaction.domain.enums.ReactionType

data class PostReactCommand(
    val userId: UserId,
    val postId: PostId,
    val reactionType: ReactionType,
) {

    fun toEntity() = PostReaction.create(userId, postId, reactionType)
}