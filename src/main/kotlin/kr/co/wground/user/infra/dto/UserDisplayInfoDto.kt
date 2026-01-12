package kr.co.wground.user.infra.dto

import kr.co.wground.global.common.UserId

data class UserDisplayInfoDto(
    val userId: UserId,
    val name: String,
    val profileImageUrl: String,
    val trackName: String,
)
