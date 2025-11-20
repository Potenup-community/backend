package kr.co.wground.api.user.service.dto

data class GoogleRequestDto(
    val email: String,
    val name: String,
    val provider: String
)
