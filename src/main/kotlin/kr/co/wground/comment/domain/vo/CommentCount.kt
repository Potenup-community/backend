package kr.co.wground.comment.domain.vo

import kr.co.wground.global.common.PostId

data class CommentCount(
    val postId: PostId,
    val count: Int,
)
