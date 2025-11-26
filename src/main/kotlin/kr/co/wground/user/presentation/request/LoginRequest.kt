package kr.co.wground.user.presentation.request

import jakarta.validation.constraints.NotBlank

class LoginRequest (
    @field:NotBlank(message = "ID 토큰은 필수입니다.")
    val idToken : String
)