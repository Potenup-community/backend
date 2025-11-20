package kr.co.wground.api.user.service.dto

data class GoogleRequestDto(
    val email: String,
    val name: String,
    val provider: String,
    val phoneNumber: String? = null,
    val affiliationId: Long? = null
)
