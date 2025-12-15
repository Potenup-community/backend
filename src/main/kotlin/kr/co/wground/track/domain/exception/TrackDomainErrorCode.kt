package kr.co.wground.track.domain.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class TrackDomainErrorCode(
    override val message: String,
    override val httpStatus: HttpStatus,
    override val code: String
) : ErrorCode {
    INVALID_DATE_RANGE("시작 일자는 종료 일자이후가 될 수 없습니다.", HttpStatus.BAD_REQUEST, "T-0002"),
    TRACK_NAME_IS_BLANK("트랙의 이름은 빈칸이 될 수 없습니다.", HttpStatus.BAD_REQUEST, "T-0003"),
    INVALID_TRACK_INPUT("트랙 입력값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST, "T-0004"),
}
