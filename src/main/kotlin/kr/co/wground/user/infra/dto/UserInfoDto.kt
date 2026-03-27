package kr.co.wground.user.infra.dto

import kr.co.wground.global.common.TrackId
import kr.co.wground.global.common.UserId
import kr.co.wground.track.domain.constant.TrackStatus
import kr.co.wground.track.domain.constant.TrackType
import kr.co.wground.track.domain.constant.toDisplayName
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserSignupStatus
import kr.co.wground.user.domain.constant.UserStatus
import java.time.LocalDateTime

data class UserInfoDto(
    val userId: UserId,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val trackId: TrackId,
    val trackName: String,
    val role: UserRole,
    val status: UserStatus,
    val requestStatus: UserSignupStatus,
    val trackStatus: TrackStatus,
    val provider: String,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
) {
    private companion object {
        const val NOT_ASSOCIATE = "소속 없음"
    }

    constructor(
        userId: UserId,
        name: String,
        email: String,
        phoneNumber: String,
        trackId: TrackId,
        trackType: TrackType?,
        cardinal: Int?,
        role: UserRole,
        status: UserStatus,
        requestStatus: UserSignupStatus,
        trackStatus: TrackStatus,
        provider: String,
        createdAt: LocalDateTime,
        modifiedAt: LocalDateTime,
    ) : this(
        userId = userId,
        name = name,
        email = email,
        phoneNumber = phoneNumber,
        trackId = trackId,
        trackName = trackType.toDisplayName(cardinal, NOT_ASSOCIATE),
        role = role,
        status = status,
        requestStatus = requestStatus,
        trackStatus = trackStatus,
        provider = provider,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
    )
}
