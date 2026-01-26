package kr.co.wground.user.presentation.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserSignupStatus
import kr.co.wground.user.domain.constant.UserStatus

@Schema(description = "유저 검색 조건")
data class UserSearchRequest(
    @field:Schema(description = "검색할 이름", example = "홍길동", required = false)
    val name: String? = null,

    @field:Schema(description = "검색할 이메일", example = "gildong@depth.co.kr", required = false)
    val email: String? = null,

    @field:Schema(description = "과정(Track) ID", example = "1", required = false)
    val trackId: Long? = null,

    @field:Schema(description = "권한 (MEMBER, INSTRUCTOR ,ADMIN)", example = "MEMBER", required = false)
    val role: UserRole? = null,

    @field:Schema(description = "유저 상태 (ACTIVE, BLOCKED 등)", example = "BLOCKED", required = false)
    val status: UserStatus? = null,

    @field:Schema(description = "가입 요청 상태 (PENDING, ACCEPTED 등)", example = "PENDING", required = false)
    val requestStatus: UserSignupStatus? = null,

    @field:Schema(description = "OAuth 제공자", example = "GOOGLE", required = false)
    val provider : String? = null,

    @field:Schema(description = "수료 상태 (true, false)", example = "false", required = false)
    val isGraduated: Boolean? = null,
)
