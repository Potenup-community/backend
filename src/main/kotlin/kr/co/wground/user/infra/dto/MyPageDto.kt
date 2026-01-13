package kr.co.wground.user.infra.dto

import kr.co.wground.global.common.TrackId
import kr.co.wground.global.common.UserId
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
)
