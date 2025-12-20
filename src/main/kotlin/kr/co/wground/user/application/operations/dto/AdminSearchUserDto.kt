package kr.co.wground.user.application.operations.dto

import kr.co.wground.global.common.UserId
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserSignupStatus
import kr.co.wground.user.domain.constant.UserStatus
import java.time.LocalDateTime

data class AdminSearchUserDto(
    val userId: UserId,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val trackName: String,
    val role: UserRole,
    val status: UserStatus,
    val provider: String,
    val requestStatus: UserSignupStatus?,
    val createdAt: LocalDateTime
)