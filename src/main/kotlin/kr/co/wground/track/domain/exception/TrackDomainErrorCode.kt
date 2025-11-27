package kr.co.wground.track.domain.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class TrackDomainErrorCode(
    override val message: String,
    override val httpStatus: HttpStatus,
    override val errorCode: String
): ErrorCode {
    INVALID_DATE_RANGE("Invalid date range", HttpStatus.BAD_REQUEST, "400 Bad Request"),
    TRACK_NAME_IS_BLANK("TrackName is blank", HttpStatus.BAD_REQUEST, "400 Bad Request"),
}