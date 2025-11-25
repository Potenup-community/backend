package kr.co.wground.like.presentation.request

import kr.co.wground.like.application.dto.LikeDto
import kr.co.wground.like.domain.PostId
import kr.co.wground.like.domain.UserId
import kr.co.wground.like.domain.enums.LikeAction

data class LikeRequest(
    val postId: PostId,
    val action: LikeAction,
) {
    fun toDto(userId: UserId) = LikeDto(
        userId = userId,
        postId = postId,
        action = action,
    )
}
