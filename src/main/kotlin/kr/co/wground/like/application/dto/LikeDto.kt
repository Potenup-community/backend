package kr.co.wground.like.application.dto

import kr.co.wground.like.domain.Like
import kr.co.wground.like.domain.PostId
import kr.co.wground.like.domain.UserId

data class LikeDto(
    val userId: UserId,
    val postId: PostId,
    val liked: Boolean,
) {
    fun toDomain() = Like(userId, postId)
}
