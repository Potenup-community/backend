package kr.co.wground.reaction.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class ReactionErrorCode(
    override val httpStatus: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {
    USER_ID_IS_NEGATIVE(HttpStatus.BAD_REQUEST, "PR-0005", "userId 가 음수입니다."),
    POST_ID_IS_NEGATIVE(HttpStatus.BAD_REQUEST, "PR-0006", "postId 가 음수입니다."),
    COMMENT_ID_IS_NEGATIVE(HttpStatus.BAD_REQUEST, "PR-0007", "commentId 가 음수입니다."),

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "PR-0008", "반응할 게시글을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PR-0009", "반응할 댓글을 찾을 수 없습니다."),
}