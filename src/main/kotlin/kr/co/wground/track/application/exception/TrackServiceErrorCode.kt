package kr.co.wground.track.application.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class TrackServiceErrorCode(
    override val message: String,
    override val httpStatus: HttpStatus,
    override val code: String
) : ErrorCode {
    TRACK_NOT_FOUND("해당 과정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "T-0001"),
}
