package kr.co.wground.exception

import org.springframework.http.HttpStatus

enum class CommonErrorCode(
    override val message: String,
    override val httpStatus: HttpStatus,
    override val code: String
) : ErrorCode {
    //Validation
    INVALID_INPUT("요청 값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST, "V-0001"),

    //AccessDenied
    ACCESS_DENIED_ROLE("접근 권한이 없습니다.", HttpStatus.FORBIDDEN, "A-0001"),

    // Server Error
    INTERNAL_SERVER_ERROR("서버 측에 오류가 발생하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR, "I-0001")
}
