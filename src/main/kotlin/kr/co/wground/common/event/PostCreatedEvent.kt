package kr.co.wground.common.event

import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.WriterId

data class PostCreatedEvent(
    val postId: PostId,
    val writerId: WriterId,
)
