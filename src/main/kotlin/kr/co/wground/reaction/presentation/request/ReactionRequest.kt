package kr.co.wground.reaction.presentation.request

import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.application.dto.CommentReactCommand
import kr.co.wground.reaction.application.dto.PostReactCommand
import kr.co.wground.reaction.domain.enums.ReactionType
import kr.co.wground.reaction.presentation.request.ReactionTarget.COMMENT
import kr.co.wground.reaction.presentation.request.ReactionTarget.POST

data class ReactionRequest(
    val targetType: ReactionTarget,
    val targetId: Long,
    val reactionType: ReactionType,
) {
    fun toPostReactCommand(userId: UserId) : PostReactCommand {
        if (targetType != POST) {
            throw IllegalStateException("targetType 이 POST 가 아닌 경우 PostReactCommand 로 변환할 수 없습니다.")
        }
        return PostReactCommand(userId = userId, postId = targetId, reactionType = reactionType)
    }

    fun toCommentReactCommand(userId: UserId) : CommentReactCommand {
        if (targetType != COMMENT) {
            throw IllegalStateException("targetType 이 COMMENT 가 아닌 경우 CommentReactCommand 로 변환할 수 없습니다.")
        }
        return CommentReactCommand(userId = userId, commentId = targetId, reactionType = reactionType)
    }
}
