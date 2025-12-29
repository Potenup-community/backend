package kr.co.wground.global.jwt.constant

enum class TokenType(
    val tokenType: String,
) {
    ACCESS("accessToken"),
    REFRESH("refreshToken"),
}