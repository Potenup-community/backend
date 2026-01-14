package kr.co.wground.reaction.application.dto

import java.time.LocalDateTime
import kr.co.wground.global.common.CommentId

data class LikedCommentDto(
    val commentId: CommentId,
    val likedAt: LocalDateTime,
)

