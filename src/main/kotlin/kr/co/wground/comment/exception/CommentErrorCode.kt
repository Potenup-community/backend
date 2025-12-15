package kr.co.wground.comment.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class CommentErrorCode(
    override val message: String,
    override val httpStatus: HttpStatus,
    override val code: String,
) : ErrorCode {
    CONTENT_IS_EMPTY("댓글 내용을 입력해주세요.", HttpStatus.BAD_REQUEST, "C-0001"),
    CONTENT_IS_TOO_LONG("댓글은 2000자를 초과할 수 없습니다.", HttpStatus.BAD_REQUEST, "C-0002"),
    COMMENT_REPLY_NOT_ALLOWED("대댓글을 작성할 수 없습니다.", HttpStatus.BAD_REQUEST, "C-0003"),

    COMMENT_NOT_FOUND("댓글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "C-0004"),
    COMMENT_PARENT_ID_NOT_FOUND("댓글 부모 ID를 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "C-0005"),

    COMMENT_NOT_WRITER("댓글 작성자가 아닙니다.", HttpStatus.FORBIDDEN, "C-0006"),
    INVALID_COMMENT_INPUT("댓글 입력값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST, "C-0007"),
}
