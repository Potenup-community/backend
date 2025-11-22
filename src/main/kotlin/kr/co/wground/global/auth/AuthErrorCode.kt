package kr.co.wground.global.auth

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class AuthErrorCode(
    override val message: String,
    override val httpStatus: HttpStatus,
    override val errorCode: String
) : ErrorCode {
    INVALID_TOKEN("INVALID_TOKEN", HttpStatus.UNAUTHORIZED,"400 Bad Request"),
    TOKEN_HAS_NOT_VALID_EMAIL("Token does not have a valid email", httpStatus = HttpStatus.FORBIDDEN,"403 Forbidden"),
}
