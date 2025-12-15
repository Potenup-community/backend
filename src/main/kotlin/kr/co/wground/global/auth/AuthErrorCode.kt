package kr.co.wground.global.auth

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class AuthErrorCode(
    override val message: String,
    override val httpStatus: HttpStatus,
    override val code: String
) : ErrorCode {
    MALFORMED_TOKEN("잘못된 형식 또는 잘못된 토큰 형식입니다", HttpStatus.BAD_REQUEST, "G-0001"),
    GOOGLE_SERVER_ERROR("Google ID 서버에 연결하지 못했습니다", HttpStatus.BAD_GATEWAY, "G-0002"),
    INVALID_SIGNATURE("토큰 서명 검증 실패했습니다.", HttpStatus.UNAUTHORIZED, "G-0003"),
    TOKEN_EXPIRED_OR_INVALID("토큰이 만료되었거나 유효하지 않습니다", HttpStatus.UNAUTHORIZED, "G-0004"),
    TOKEN_HAS_NOT_VALID_EMAIL("토큰에 유효한 이메일이 없습니다", HttpStatus.NOT_FOUND, "G-0005"),
}
