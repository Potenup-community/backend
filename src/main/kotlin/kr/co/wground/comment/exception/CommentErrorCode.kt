package kr.co.wground.comment.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class CommentErrorCode(
    override val message: String,
    override val httpStatus: HttpStatus,
    override val errorCode: String,
) : ErrorCode {
    CONTENT_IS_EMPTY("댓글 내용을 입력해주세요.", HttpStatus.BAD_REQUEST, "C-001"),
    CONTENT_IS_TOO_LONG("댓글은 2000자를 초과할 수 없습니다.", HttpStatus.BAD_REQUEST, "C-002"),
    COMMENT_REPLY_NOT_ALLOWED("대댓글을 작성할 수 없습니다.", HttpStatus.BAD_REQUEST, "C-003"),

    COMMENT_NOT_FOUND("댓글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "C-004"),
    COMMENT_PARENT_ID_NOT_FOUND("댓글 부모 ID를 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "C-005"),

    COMMENT_NOT_WRITER("댓글 작성자가 아닙니다.", HttpStatus.FORBIDDEN, "C-006"),
}
