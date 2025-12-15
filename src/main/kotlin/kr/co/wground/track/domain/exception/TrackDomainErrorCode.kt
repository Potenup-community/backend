package kr.co.wground.track.domain.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class TrackDomainErrorCode(
    override val httpStatus: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "T-0002", "시작 일자는 종료 일자이후가 될 수 없습니다."),
    TRACK_NAME_IS_BLANK(HttpStatus.BAD_REQUEST, "T-0003", "트랙의 이름은 빈칸이 될 수 없습니다."),
    INVALID_TRACK_INPUT(HttpStatus.BAD_REQUEST, "T-0004", "트랙 입력값이 올바르지 않습니다."),
}
