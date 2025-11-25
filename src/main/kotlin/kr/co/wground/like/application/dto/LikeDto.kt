package kr.co.wground.like.application.dto

import kr.co.wground.like.domain.Like
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import kr.co.wground.like.domain.enums.LikeAction

data class LikeDto(
    val userId: UserId,
    val postId: PostId,
    val action: LikeAction,
) {
    fun toDomain() = Like(userId, postId)
}
