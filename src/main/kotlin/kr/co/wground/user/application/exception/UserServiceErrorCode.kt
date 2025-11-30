package kr.co.wground.user.application.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class UserServiceErrorCode(
    override val message: String,
    override val httpStatus: HttpStatus,
    override val errorCode: String
) : ErrorCode {
    REQUEST_SIGNUP_NOT_FOUND("해당 가입요청을 찾을 수 없습니다.",HttpStatus.NOT_FOUND,"U-0001"),
    REQUEST_SIGNUP_ALREADY_EXISTED("이미 가입 요청한 유저 입니다.",HttpStatus.BAD_REQUEST,"U-0002"),
    AUTHENTICATION_NOT_FOUND("인증 정보를 찾을 수 없습니다.",HttpStatus.BAD_REQUEST, "U-0003"),
    APPROVE_NECESSARY_ROLE("권한 허가가 필요합니다.",HttpStatus.BAD_REQUEST,"U-0004"),
    ALREADY_SIGNED_USER("이미 가입된 유저 입니다.",HttpStatus.BAD_REQUEST,"U-0005"),
    USER_NOT_FOUND("유저를 찾을 수 없습니다.",HttpStatus.NOT_FOUND,"U-0006"),
    ROLE_ADMIN_CANT_REQUEST("관리자 권한은 요청 될 수 없습니다.", HttpStatus.BAD_REQUEST,"U-0007"),
    INACTIVE_USER("유저가 활성화 되지않았습니다.",HttpStatus.BAD_REQUEST,"U-0008"),
    INVALID_REFRESH_TOKEN("리프레시 토큰이 유효하지 않습니다.",HttpStatus.UNAUTHORIZED,"U-0009"),
    TOKEN_EXPIRED("엑세스 토큰이 만료되었습니다.",HttpStatus.UNAUTHORIZED, "U-0010"),

    ;
}
