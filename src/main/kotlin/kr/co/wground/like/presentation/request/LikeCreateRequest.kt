package kr.co.wground.like.presentation.request

import kr.co.wground.like.application.dto.LikeCreateDto
import kr.co.wground.like.domain.PostId
import kr.co.wground.like.domain.UserId

data class LikeCreateRequest(
    val userId: UserId,
    val postId: PostId,
) {
    fun toDto(userId: UserId) = LikeCreateDto(
        userId = userId,
        postId = postId,
    )
}
