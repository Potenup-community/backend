package kr.co.wground.reaction.presentation.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.application.dto.CommentReactCommand
import kr.co.wground.reaction.application.dto.PostReactCommand
import kr.co.wground.reaction.domain.enums.ReactionType
import kr.co.wground.reaction.presentation.request.ReactionTarget.COMMENT
import kr.co.wground.reaction.presentation.request.ReactionTarget.POST

@Schema(description = "리액션 생성 및 취소 요청 바디")
data class ReactionRequest(

    @field:Schema(example = "POST")
    @field:NotNull(message = "반응할 대상의 유형을 명시해주세요(ex. POST, COMMENT, ...)")
    val targetType: ReactionTarget,

    @field:Schema(example = "1")
    @field:NotNull(message = "targetId 가 null 입니다.")
    @field:Positive(message = "대상의 id 는 0 또는 음수일 수 없습니다.")
    val targetId: Long,

    @field:Schema(example = "LIKE")
    @field:NotNull(message = "반응 유형을 명시해주세요(ex. LIKE, HEART, ...)")
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
