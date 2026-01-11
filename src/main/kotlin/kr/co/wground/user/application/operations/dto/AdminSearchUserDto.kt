package kr.co.wground.user.application.operations.dto

import io.swagger.v3.oas.annotations.media.Schema
import kr.co.wground.global.common.UserId
import kr.co.wground.track.domain.constant.TrackStatus
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserSignupStatus
import kr.co.wground.user.domain.constant.UserStatus
import java.time.LocalDateTime

@Schema(description = "관리자용 유저 검색 결과 DTO")
data class AdminSearchUserDto(
    @field:Schema(description = "유저 ID", example = "1")
    val userId: UserId,

    @field:Schema(description = "이름", example = "홍길동")
    val name: String,

    @field:Schema(description = "이메일", example = "test@example.com")
    val email: String,

    @field:Schema(description = "전화번호", example = "01012345678")
    val phoneNumber: String,

    @field:Schema(description = "트랙 이름", example = "BE 1기")
    val trackName: String,

    @field:Schema(description = "권한", example = "MEMBER")
    val role: UserRole,

    @field:Schema(description = "학적 상태", example = "ENROLLED")
    val trackStatus: TrackStatus,

    @field:Schema(description = "활동 상태", example = "ACTIVE")
    val status: UserStatus,

    @field:Schema(description = "가입 제공자", example = "GOOGLE")
    val provider: String,

    @field:Schema(description = "가입 요청 상태", example = "ACCEPTED")
    val requestStatus: UserSignupStatus,

    @field:Schema(description = "가입 일시")
    val createdAt: LocalDateTime
)