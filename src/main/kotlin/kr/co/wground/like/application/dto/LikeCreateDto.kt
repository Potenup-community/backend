package kr.co.wground.like.application.dto

import kr.co.wground.like.domain.Like
import kr.co.wground.like.domain.PostId
import kr.co.wground.like.domain.UserId

data class LikeCreateDto(
    val userId: UserId,
    val postId: PostId,
) {
    fun toDomain() = Like(userId, postId)
}
