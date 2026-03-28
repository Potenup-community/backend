package kr.co.wground.user.infra.dto

import kr.co.wground.global.common.UserId
import kr.co.wground.shop.application.dto.EquippedItem
import kr.co.wground.track.domain.constant.TrackType
import kr.co.wground.track.domain.constant.toDisplayName
import kr.co.wground.user.utils.defaultimage.domain.UserProfile

data class UserDisplayInfoDto(
    val userId: UserId,
    val name: String,
    val profileImageUrl: String,
    val trackName: String,
    val items: List<EquippedItem> = emptyList()
) {
    private companion object {
        const val NOT_ASSOCIATE = "소속 없음"
    }

    constructor(userId: UserId, name: String, userProfile: UserProfile, trackName: String) : this(
        userId = userId,
        name = name,
        profileImageUrl = userProfile.getAccessUrl(),
        trackName = trackName,
    )

    constructor(userId: UserId, name: String, userProfile: UserProfile, trackType: TrackType?, cardinal: Int?) : this(
        userId = userId,
        name = name,
        profileImageUrl = userProfile.getAccessUrl(),
        trackName = trackType.toDisplayName(cardinal, NOT_ASSOCIATE),
    )
}
