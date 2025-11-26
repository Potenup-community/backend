package kr.co.wground.user.presentation.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class SignUpRequest(
    @field:NotBlank(message = "ID 토큰은 필수입니다.")
    val idToken: String,
    @field:NotNull(message = "트랙 ID는 필수입니다.")
    val trackId: Long,
    @field:NotBlank(message = "이름은 필수입니다.")
    @field:Size(max = 10, message = "이름은 10자 이내로 입력해주세요.")
    val name : String,
    @field:NotBlank(message = "전화번호는 필수입니다.")
    val phoneNumber: String,
    @field:NotBlank(message = "제공자 정보는 필수입니다.")
    val provider: String,
)
