package kr.co.wground.comment.presentation.response

import io.swagger.v3.oas.annotations.media.Schema

data class CommentsResponse(
    @field:Schema(description = "댓글 목록")
    val contents: List<CommentSummaryResponse>,
)
