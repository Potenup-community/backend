package kr.co.wground.reaction.application.dto

import kr.co.wground.reaction.domain.PostReaction
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.domain.enums.ReactionType

// To Do: 리액션 대상 별로 DTO 분리해야 함(ex. PostReactionDto, CommentReactionDto, ...)
data class ReactionDto(
    val userId: UserId,
    val postId: PostId,
    val reactionType: ReactionType,
) {
    fun toDomain() = PostReaction.create(userId, postId, reactionType)
}
