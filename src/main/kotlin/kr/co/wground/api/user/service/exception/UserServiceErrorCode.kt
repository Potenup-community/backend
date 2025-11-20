package kr.co.wground.api.user.service.exception

import kr.co.wground.api.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class UserServiceErrorCode(
    override val message: String,
    override val httpStatus: HttpStatus,
    override val errorCode: String
) : ErrorCode {
    REQUEST_SIGNUP_NOT_FOUND("Request signup not found",HttpStatus.NOT_FOUND,""),
    REQUEST_SIGNUP_ALREADY_EXISTED("Request signup already exists",HttpStatus.UNPROCESSABLE_ENTITY,""),


}