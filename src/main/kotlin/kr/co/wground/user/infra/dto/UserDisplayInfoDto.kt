package kr.co.wground.user.infra.dto

import kr.co.wground.global.common.UserId
import kr.co.wground.user.utils.defaultimage.domain.UserProfile

data class UserDisplayInfoDto(
    val userId: UserId,
    val name: String,
    val profileImageUrl: String,
    val trackName: String,
) {
    constructor(userId: UserId, name: String, userProfile: UserProfile, trackName: String) : this(
        userId = userId,
        name = name,
        profileImageUrl = userProfile.getAccessUrl(),
        trackName = trackName
    )
}
