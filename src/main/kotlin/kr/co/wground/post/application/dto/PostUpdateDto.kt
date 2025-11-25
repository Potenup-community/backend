package kr.co.wground.post.application.dto

import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId

data class PostUpdateDto(
    val id: PostId,
    val title: String? = null,
    val content: String? = null,
    val writerId: UserId,
    val topic: Topic? = null,
) {

}
