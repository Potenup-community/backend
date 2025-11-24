package kr.co.wground.like.exception

import kr.co.wground.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class LikeErrorCode(
    override val message: String,
    override val httpStatus: HttpStatus,
    override val errorCode: String,
) : ErrorCode {
}
