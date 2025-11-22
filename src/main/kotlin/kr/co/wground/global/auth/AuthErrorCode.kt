package kr.co.wground.global.auth

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class AuthErrorCode(
    override val message: String,
    override val httpStatus: HttpStatus,
    override val errorCode: String
) : ErrorCode {
    INVALID_TOKEN("INVALID_TOKEN", HttpStatus.UNAUTHORIZED,"400 Bad Request"),
}