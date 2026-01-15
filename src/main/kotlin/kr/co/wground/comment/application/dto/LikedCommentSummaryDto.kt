package kr.co.wground.comment.application.dto

import java.time.LocalDateTime
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.application.dto.CommentReactionStats

data class LikedCommentSummaryDto(
    val commentId: CommentId,
    val postId: PostId,
    val content: String,
    val authorId: UserId,
    val authorName: String,
    val trackName: String,
    val authorProfileImageUrl: String,
    val createdAt: LocalDateTime,
    val likedAt: LocalDateTime,
    val commentReactionStats: CommentReactionStats,
    val isDeleted: Boolean,
)

