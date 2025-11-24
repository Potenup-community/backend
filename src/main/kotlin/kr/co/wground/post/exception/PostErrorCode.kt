package kr.co.wground.post.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class PostErrorCode(
    override val message: String,
    override val httpStatus: HttpStatus,
    override val errorCode: String,
): ErrorCode {
    NOT_FOUND_POST("해당 게시글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "P-0001"),
}
