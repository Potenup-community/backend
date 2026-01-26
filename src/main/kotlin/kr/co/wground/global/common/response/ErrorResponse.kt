package kr.co.wground.global.common.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.co.wground.exception.ErrorCode

@Schema(description = "표준 에러 응답")
data class ErrorResponse private constructor(
    @field:Schema(example = "INVALID_ARGUMENT")
    val code: String,
    @field:Schema(example = "요청값이 올바르지 않습니다.")
    val message: String,
    @field:Schema(description = "필드 검증 오류 목록(Validation일 때만 존재)")
    val errors: List<CustomError>,
) {
    data class CustomError(
        val field: String,
        val reason: String,
    )

    companion object {
        fun of(errorCode: ErrorCode, errors: List<CustomError> = emptyList(), additionalInfo: String = ""): ErrorResponse {
            return ErrorResponse(
                code = errorCode.code,
                message = errorCode.message + if (additionalInfo.isEmpty()) "" else ", $additionalInfo",
                errors = errors,
            )
        }
    }
}
