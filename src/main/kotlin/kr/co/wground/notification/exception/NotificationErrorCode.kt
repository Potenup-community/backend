package kr.co.wground.notification.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class NotificationErrorCode (
    override val httpStatus: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {
    INVALID_NOTIFICATION_INPUT(HttpStatus.BAD_REQUEST, "N-0001", "알림의 입력값이 올바르지 않습니다."),
}
