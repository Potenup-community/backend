package kr.co.wground.comment.application.dto

data class CommentUpdateDto(
    val commentId: Long,
    val content: String?
)
