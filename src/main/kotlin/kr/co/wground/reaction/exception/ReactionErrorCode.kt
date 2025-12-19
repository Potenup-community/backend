package kr.co.wground.reaction.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class ReactionErrorCode(
    override val httpStatus: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {
    USER_ID_IS_NULL(HttpStatus.BAD_REQUEST, "PR-0001", "userId 가 null 입니다."),
    POST_ID_IS_NULL(HttpStatus.BAD_REQUEST, "PR-0002", "postId 가 null 입니다."),
    COMMENT_ID_IS_NULL(HttpStatus.BAD_REQUEST, "PR-0003", "commentId 가 null 입니다."),
    REACTION_TYPE_IS_NULL(HttpStatus.BAD_REQUEST, "PR-0004", "reactionType 이 null 입니다."),

    USER_ID_IS_NEGATIVE(HttpStatus.BAD_REQUEST, "PR-0005", "userId 가 음수입니다."),
    POST_ID_IS_NEGATIVE(HttpStatus.BAD_REQUEST, "PR-0006", "postId 가 음수입니다."),
    COMMENT_ID_IS_NEGATIVE(HttpStatus.BAD_REQUEST, "PR-0007", "commentId 가 음수입니다."),

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "PR-0008", "반응할 게시글을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PR-0009", "반응할 댓글을 찾을 수 없습니다."),
}