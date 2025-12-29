package kr.co.wground.track.application.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class TrackServiceErrorCode(
    override val httpStatus: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {
    TRACK_NOT_FOUND(HttpStatus.NOT_FOUND, "T-0001", "해당 과정을 찾을 수 없습니다."),
}
