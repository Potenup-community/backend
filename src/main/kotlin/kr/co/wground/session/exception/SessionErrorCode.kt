package kr.co.wground.session.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class SessionErrorCode(
    override val httpStatus: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND,       "SS-0001", "세션을 찾을 수 없습니다."),
    SESSION_FORBIDDEN(HttpStatus.FORBIDDEN,        "SS-0002", "해당 세션에 접근 권한이 없습니다."),
    SESSION_INACTIVE(HttpStatus.UNAUTHORIZED,      "SS-0003", "비활성 세션입니다. 다시 로그인해 주세요."),
    SESSION_ALREADY_INACTIVE(HttpStatus.BAD_REQUEST, "SS-0004", "이미 비활성화된 세션입니다."),
}
