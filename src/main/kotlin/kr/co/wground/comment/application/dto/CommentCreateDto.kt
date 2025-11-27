package kr.co.wground.comment.application.dto

import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.PostId
import kr.co.wground.global.config.resolver.CurrentUserId

data class CommentCreateDto(
    val writerId: CurrentUserId,
    val postId: PostId,
    val parentId: CommentId? = null,
    val content: String,
)
