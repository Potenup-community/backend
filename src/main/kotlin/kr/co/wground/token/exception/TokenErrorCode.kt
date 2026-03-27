package kr.co.wground.token.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class TokenErrorCode(
    override val httpStatus: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {
    USER_REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "TK-0001", "RefreshToken이 존재하지 않습니다.")
}