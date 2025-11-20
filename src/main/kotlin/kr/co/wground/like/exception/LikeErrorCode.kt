package kr.co.wground.like.exception

import kr.co.wground.api.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class LikeErrorCode(
    override val message: String,
    override val httpStatus: HttpStatus,
    override val errorCode: String
) : ErrorCode {
    ALREADY_LIKED("이미 '좋아요'를 누른 게시물입니다.", HttpStatus.BAD_REQUEST, "L001")
}
