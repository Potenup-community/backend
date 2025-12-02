package kr.co.wground.global.auth

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class AuthErrorCode(
    override val message: String,
    override val httpStatus: HttpStatus,
    override val errorCode: String
) : ErrorCode {
    MALFORMED_TOKEN("잘못된 형식 또는 잘못된 토큰 형식입니다", HttpStatus.BAD_REQUEST, "400 Bad Request"),
    GOOGLE_SERVER_ERROR("Google ID 서버에 연결하지 못했습니다", HttpStatus.BAD_GATEWAY, "502 Bad Gateway"),
    INVALID_SIGNATURE("토큰 서명 검증 실패했습니다.", HttpStatus.UNAUTHORIZED, "401 Unauthorized"),
    TOKEN_EXPIRED_OR_INVALID("토큰이 만료되었거나 유효하지 않습니다", HttpStatus.UNAUTHORIZED, "401 Unauthorized"),
    TOKEN_HAS_NOT_VALID_EMAIL("토큰에 유효한 이메일이 없습니다", HttpStatus.NOT_FOUND,"404 Not Found"),
}
