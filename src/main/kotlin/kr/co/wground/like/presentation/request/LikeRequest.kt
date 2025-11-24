package kr.co.wground.like.presentation.request

import kr.co.wground.like.application.dto.LikeDto
import kr.co.wground.like.domain.PostId
import kr.co.wground.like.domain.UserId

data class LikeRequest(
    val postId: PostId,
    val liked: Boolean,
) {
    fun toDto(userId: UserId) = LikeDto(
        userId = userId,
        postId = postId,
        liked = liked
    )
}
