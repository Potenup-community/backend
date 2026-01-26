package kr.co.wground.common.event

import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import kr.co.wground.global.common.WriterId

data class CommentReactionCreatedEvent(
    val postId: PostId,
    val commentId: CommentId,
    val commentWriterId: WriterId,
    val reactorId: UserId,
)
