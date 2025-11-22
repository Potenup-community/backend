package kr.co.wground.global.auth

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class AuthErrorCode(
    override val message: String,
    override val httpStatus: HttpStatus,
    override val errorCode: String
) : ErrorCode {
    INVALID_TOKEN("INVALID_TOKEN", HttpStatus.UNAUTHORIZED,"400 Bad Request"),
    MALFORMED_TOKEN("Malformed or invalid token format", HttpStatus.BAD_REQUEST, "400 Bad Request"),
    GOOGLE_SERVER_ERROR("Failed to connect to Google Identity Server", HttpStatus.INTERNAL_SERVER_ERROR, "500 Internal Server Error"),
    INVALID_SIGNATURE("Token signature validation failed", HttpStatus.UNAUTHORIZED, "401 Unauthorized"),
    TOKEN_EXPIRED_OR_INVALID("Token is expired or invalid", HttpStatus.UNAUTHORIZED, "401 Unauthorized"),
    TOKEN_HAS_NOT_VALID_EMAIL("Token does not have a valid email", httpStatus = HttpStatus.FORBIDDEN,"403 Forbidden"),
}
