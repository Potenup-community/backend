package kr.co.wground.notification.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class NotificationErrorCode(
    override val httpStatus: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {
    INVALID_NOTIFICATION_INPUT(HttpStatus.BAD_REQUEST, "N-0001", "알림의 입력값이 올바르지 않습니다."),
    INVALID_NOTIFICATION_REFERENCE(HttpStatus.BAD_REQUEST, "N-0002", "알림의 참조 정보가 올바르지 않습니다."),
    INVALID_RECIPIENT_ID(HttpStatus.BAD_REQUEST, "N-0003", "수신자 ID가 올바르지 않습니다."),
    INVALID_ACTOR_ID(HttpStatus.BAD_REQUEST, "N-0004", "행위자 ID가 올바르지 않습니다."),
    INVALID_EXPIRES_AT(HttpStatus.BAD_REQUEST, "N-0005", "만료일은 생성일 이후여야 합니다."),

    DUPLICATE_NOTIFICATION(HttpStatus.CONFLICT, "N-0006", "이미 처리된 알림입니다."),

    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "N-0007", "알림을 찾을 수 없습니다."),

    TEMPLATE_NOT_FOUND(HttpStatus.BAD_REQUEST, "N-0008", "알림 메시지 템플릿을 찾을 수 없습니다."),

    INVALID_BROADCAST_TARGET(HttpStatus.BAD_REQUEST, "N-0009", "브로드캐스트 대상 정보가 올바르지 않습니다."),
}
