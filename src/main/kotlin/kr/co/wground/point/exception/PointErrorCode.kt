package kr.co.wground.point.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class PointErrorCode(
    override val httpStatus: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {
    // Wallet 관련
    WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "PT-0001", "포인트 지갑을 찾을 수 없습니다."),
    NOT_SUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "PT-0002", "포인트 잔액이 부족합니다."),

    // History 관련
    HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "PT-0003", "포인트 내역을 찾을 수 없습니다."),
    DUPLICATE_POINT_HISTORY(HttpStatus.CONFLICT, "PT-0004", "이미 처리된 포인트 내역입니다."),

    // 유효성 검증
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "PT-0005", "유효하지 않은 사용자 ID입니다."),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "PT-0006", "포인트 금액은 0보다 커야 합니다."),
    INVALID_REF_ID(HttpStatus.BAD_REQUEST, "PT-0007", "참조 ID는 0보다 커야 합니다."),
    INVALID_POINT_TYPE(HttpStatus.BAD_REQUEST, "PT-0008", "유효하지 않은 포인트 타입입니다."),

    // 일일 한도
    DAILY_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "PT-0009", "일일 포인트 획득 한도를 초과했습니다."),
    ATTENDANCE_ALREADY_CHECKED(HttpStatus.CONFLICT, "PT-0010", "오늘 이미 출석 체크를 완료했습니다."),

    POINT_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PT-0011", "포인트 처리 중 오류가 발생했습니다."),

    // 권한
    ADMIN_POINT_GRANT_UNAUTHORIZED(HttpStatus.FORBIDDEN, "PT-0013", "관리자 포인트 지급 권한이 없습니다."),
}