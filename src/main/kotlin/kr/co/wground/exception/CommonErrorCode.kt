package kr.co.wground.exception

import org.springframework.http.HttpStatus

enum class CommonErrorCode(
    override val httpStatus: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {
    //Validation
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "V-0001", "요청 값이 올바르지 않습니다."),

    //AccessDenied
    ACCESS_DENIED_ROLE(HttpStatus.FORBIDDEN, "A-0001", "접근 권한이 없습니다."),
    AUTHORIZATION_FAILURE(HttpStatus.UNAUTHORIZED,"A-0002","인증에 실패하였습니다."),

    // Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "I-0001", "서버 측에 오류가 발생하였습니다."),
}
