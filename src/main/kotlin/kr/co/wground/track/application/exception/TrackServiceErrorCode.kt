package kr.co.wground.track.application.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class TrackServiceErrorCode(
    override val message: String,
    override val httpStatus: HttpStatus,
    override val errorCode: String
): ErrorCode {
    TRACK_NOT_FOUND("Track not found", HttpStatus.NOT_FOUND,"404 Not Found"),
}
