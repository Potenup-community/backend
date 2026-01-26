package kr.co.wground.user.presentation.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "로그인 요청 데이터")
class LoginRequest (
    @field:Schema(description = "OAuth2 ID 토큰", example = "eyJhbGciOiJSUzI1NiIs...")
    @field:NotBlank(message = "ID 토큰은 필수입니다.")
    val idToken : String
)