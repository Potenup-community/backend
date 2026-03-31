package kr.co.wground.user.infra.dto

import kr.co.wground.global.common.TrackId
import kr.co.wground.global.common.UserId
import kr.co.wground.track.domain.constant.TrackType
import kr.co.wground.track.domain.constant.toDisplayName
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserStatus
import kr.co.wground.user.utils.defaultimage.domain.UserProfile

data class MyPageDto(
    val userId: UserId,
    val name: String,
    val email: String,
    val trackId: TrackId,
    val trackName: String,
    val userProfile: UserProfile,
    val role: UserRole,
    val status: UserStatus,
) {
    private companion object {
        const val NOT_ASSOCIATE = "소속 없음"
    }

    constructor(
        userId: UserId,
        name: String,
        email: String,
        trackId: TrackId,
        trackType: TrackType?,
        cardinal: Int?,
        userProfile: UserProfile,
        role: UserRole,
        status: UserStatus,
    ) : this(
        userId = userId,
        name = name,
        email = email,
        trackId = trackId,
        trackName = trackType.toDisplayName(cardinal, NOT_ASSOCIATE),
        userProfile = userProfile,
        role = role,
        status = status,
    )
}
