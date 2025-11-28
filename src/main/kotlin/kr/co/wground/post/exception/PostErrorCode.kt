package kr.co.wground.post.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class PostErrorCode(
    override val message: String,
    override val httpStatus: HttpStatus,
    override val errorCode: String,
): ErrorCode {
    NOT_FOUND_POST("해당 게시글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "P-0001"),
    TITLE_TOO_LONG("게시글의 제목은 50자를 넘을 수 없습니다.", HttpStatus.BAD_REQUEST, "P-0002"),
    CONTENT_TOO_LONG("게시글의 본문은 5000자를 넘을 수 없습니다.", HttpStatus.BAD_REQUEST, "P-0003"),
    TITLE_IS_EMPTY("게시글의 제목을 넣어주세요.", HttpStatus.BAD_REQUEST, "P-0004"),
    YOU_ARE_NOT_OWNER_THIS_POST("해당 게시글의 주인이 아닙니다.", HttpStatus.BAD_REQUEST, "P-0005"),
}
