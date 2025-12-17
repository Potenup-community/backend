package kr.co.wground.comment.application.dto

import java.time.LocalDateTime
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.UserId

data class CommentSummaryDto(
    val commentId: CommentId,
    val content: String,
    val authorId: UserId,
    val authorName: String,
    val authorProfileImageUrl: String?,
    val createdAt: LocalDateTime,
    val likeCount: Int = 0,
    val isDeleted: Boolean,
    val replies: List<CommentSummaryDto>,
)
