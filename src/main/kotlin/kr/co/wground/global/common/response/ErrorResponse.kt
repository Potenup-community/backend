package kr.co.wground.global.common.response

import kr.co.wground.exception.ErrorCode

data class ErrorResponse(
    val code: String,
    val message: String,
    val errors: List<CustomError> = emptyList(),
) {
    data class CustomError(
        val field: String,
        val reason: String,
    )

    companion object {
        fun of(errorCode: ErrorCode, errors: List<CustomError>): ErrorResponse {
            return ErrorResponse(
                code = errorCode.code,
                message = errorCode.message,
                errors = errors,
            )
        }
    }
}
