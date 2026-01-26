package kr.co.wground.common.event

import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import kr.co.wground.global.common.WriterId

data class PostReactionCreatedEvent(
    val postId: PostId,
    val postWriterId: WriterId,
    val reactorId: UserId,
)
