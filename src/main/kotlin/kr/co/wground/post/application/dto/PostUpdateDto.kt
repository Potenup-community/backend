package kr.co.wground.post.application.dto

import kr.co.wground.like.domain.PostId
import kr.co.wground.like.domain.UserId

data class PostUpdateDto(
    val id: PostId,
    val title: String? = null,
    val content: String? = null,
    val writerId: UserId
) {

}
