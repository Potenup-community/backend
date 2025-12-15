package kr.co.wground.user.presentation.dto

enum class TokenType(
    val tokenType: String,
) {
    ACCESS("accessToken"),
    REFRESH("refreshToken"),
}