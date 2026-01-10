package kr.co.wground.comment.presentation.response

import kr.co.wground.global.common.UserId

data class CommentAuthorResponse(
    val userId: UserId,
    val name: String,
    val trackName: String,
    val profileImageUrl: String?,
)
