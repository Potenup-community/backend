package kr.co.wground.user.infra.dto

import kr.co.wground.global.common.TrackId
import kr.co.wground.global.common.UserId
import kr.co.wground.track.domain.constant.TrackStatus
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
)
