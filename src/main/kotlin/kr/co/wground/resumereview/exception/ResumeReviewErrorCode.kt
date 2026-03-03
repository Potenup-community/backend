package kr.co.wground.resumereview.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class ResumeReviewErrorCode(
    override val httpStatus: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {
    NOT_ALLOW_BLANK_FIELD(HttpStatus.BAD_REQUEST, "RR-0001", "(%s) 해당 필드는 비어 있을 수 없습니다."),
    TOO_LONG_FIELD(HttpStatus.BAD_REQUEST, "RR-0001", "(%s) 해당 필드는 최대 (%d)자까지 입력 가능합니다."),
    TOO_LONG_TEXT_OF_ALL_SECTIONS(HttpStatus.BAD_REQUEST, "RR-0002", "모든 섹션의 전체 글의 길이는 12000자를 넘을 수 없습니다."),
    UNEXPECTED_UNIQUE_VIOLATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "RR-0003", "UNIQUE constraint violation이 발생했는데, 그 레코드를 다시 조회했더니 없음"),
    NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, "RR-0004", "이력서 첨삭 결과를 찾을 수 없습니다.")
}
