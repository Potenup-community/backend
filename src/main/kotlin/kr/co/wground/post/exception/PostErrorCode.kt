package kr.co.wground.post.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class PostErrorCode(
    override val httpStatus: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {
    NOT_FOUND_POST(HttpStatus.NOT_FOUND, "P-0001", "해당 게시글을 찾을 수 없습니다."),
    TITLE_TOO_LONG(HttpStatus.BAD_REQUEST, "P-0002", "게시글의 제목은 50자를 넘을 수 없습니다."),
    CONTENT_TOO_LONG(HttpStatus.BAD_REQUEST, "P-0003", "게시글의 본문은 5000자를 넘을 수 없습니다."),
    TITLE_IS_EMPTY(HttpStatus.BAD_REQUEST, "P-0004", "게시글의 제목을 넣어주세요."),
    YOU_ARE_NOT_OWNER_THIS_POST(HttpStatus.FORBIDDEN, "P-0005", "해당 게시글의 주인이 아닙니다."),
    INVALID_POST_INPUT(HttpStatus.BAD_REQUEST, "P-0006", "게시글 입력값이 올바르지 않습니다."),
}
