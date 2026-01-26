package kr.co.wground.user.presentation.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Schema(description = "회원가입 요청 데이터")
data class SignUpRequest(
    @field:Schema(description = "구글 ID 토큰", example = "eyJhbGciOiJSUzI1NiIs...")
    @field:NotBlank(message = "ID 토큰은 필수입니다.")
    val idToken: String,

    @field:Schema(description = "선택한 트랙 ID", example = "1")
    @field:NotNull(message = "트랙 ID는 필수입니다.")
    val trackId: Long,

    @field:Schema(description = "사용자 실명", example = "홍길동")
    @field:NotBlank(message = "이름은 필수입니다.")
    @field:Size(max = 10, message = "이름은 10자 이내로 입력해주세요.")
    val name : String,

    @field:Schema(description = "전화번호", example = "01012345678")
    @field:NotBlank(message = "전화번호는 필수입니다.")
    val phoneNumber: String,

    @field:Schema(description = "가입 제공자 (OAuth2)", example = "GOOGLE")
    @field:NotBlank(message = "제공자 정보는 필수입니다.")
    val provider: String,
)
