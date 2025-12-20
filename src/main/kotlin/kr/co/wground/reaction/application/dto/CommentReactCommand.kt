package kr.co.wground.reaction.application.dto

import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.domain.PostReaction
import kr.co.wground.reaction.domain.enums.ReactionType

data class CommentReactCommand(
    val userId: UserId,
    val commentId: CommentId,
    val reactionType: ReactionType,
) {

    fun toEntity() = PostReaction.create(userId, commentId, reactionType)
}