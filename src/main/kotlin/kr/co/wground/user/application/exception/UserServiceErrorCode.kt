package kr.co.wground.user.application.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class UserServiceErrorCode(
    override val message: String,
    override val httpStatus: HttpStatus,
    override val errorCode: String
) : ErrorCode {
    REQUEST_SIGNUP_NOT_FOUND("Request signup not found",HttpStatus.NOT_FOUND,"400 Bad Request"),
    REQUEST_SIGNUP_ALREADY_EXISTED("Request signup already exists",HttpStatus.UNPROCESSABLE_ENTITY,"400 Bad Request"),
    INVALID_INPUT_VALUE("Invalid Input",HttpStatus.NOT_FOUND,"400 Bad Request"),
    NOT_PENDING_USER("Not pending user",HttpStatus.BAD_REQUEST,"400 Bad Request"),
    ALREADY_SIGNED_USER("Already signed user",HttpStatus.BAD_REQUEST,"400 Bad Request")
    ;

}