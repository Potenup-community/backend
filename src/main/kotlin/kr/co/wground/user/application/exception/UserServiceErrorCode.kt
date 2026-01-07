package kr.co.wground.user.application.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class UserServiceErrorCode(
    override val httpStatus: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {
    REQUEST_SIGNUP_NOT_FOUND(HttpStatus.NOT_FOUND, "U-0001", "해당 가입요청을 찾을 수 없습니다."),
    REQUEST_SIGNUP_ALREADY_EXISTED(HttpStatus.BAD_REQUEST, "U-0002", "이미 가입 요청한 유저 입니다."),
    AUTHENTICATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "U-0003", "인증 정보를 찾을 수 없습니다."),
    APPROVE_NECESSARY_ROLE(HttpStatus.BAD_REQUEST, "U-0004", "권한 허가가 필요합니다."),
    ALREADY_SIGNED_USER(HttpStatus.BAD_REQUEST, "U-0005", "이미 가입된 유저 입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U-0006", "유저를 찾을 수 없습니다."),
    ROLE_ADMIN_CANT_REQUEST(HttpStatus.BAD_REQUEST, "U-0007", "관리자 권한은 요청 될 수 없습니다."),
    INACTIVE_USER(HttpStatus.BAD_REQUEST, "U-0008", "유저가 활성화 되지않았습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "U-0009", "리프레시 토큰이 유효하지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "U-0010", "엑세스 토큰이 만료되었습니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "U-0011", "액세스 토큰이 유효하지 않습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "U-0012", "리프레시 토큰을 찾을수 없습니다."),
    INVALID_USER_INPUT(HttpStatus.BAD_REQUEST, "U-0012", "사용자 입력값이 올바르지 않습니다."),
    PAGE_NUMBER_IS_OVER_TOTAL_PAGE(HttpStatus.BAD_REQUEST,"U-0013","최대 페이지 수를 넘었습니다."),
    PAGE_NUMBER_MIN_ERROR(HttpStatus.BAD_REQUEST,"U-0014","최소 페이지 수에 해당하지 않습니다."),
    CANT_REQUEST_NEXT_PAGE_IN_ZERO_ELEMENT(HttpStatus.BAD_REQUEST,"U-0015","해당 속성에 해당되는 유저가 없습니다. 다음 페이지는 요청 될 수 없습니다."),
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND,"U-0016","해당 유저의 프로필 정보가 존재하지 않습니다."),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST,"U-0017", "대기 상태로는  다시 돌아갈 수 없습니다."),
    DUPLICATED_PHONE_NUMBER(HttpStatus.BAD_REQUEST,"U-0018","해당 전화번호는 이미 등록되어 있습니다."),
    FIND_IDS_SIZE_DIFFERENT_REQUEST_IDS_SIZE(HttpStatus.CONFLICT, "U-0019", "요청한 ID 목록 값과 조회된 ID 목록 값의 크기가 다릅니다."),
}
