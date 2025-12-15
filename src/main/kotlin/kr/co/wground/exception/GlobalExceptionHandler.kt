package kr.co.wground.exception

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ErrorResponse(
    val code: String,
    val message: String,
)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            code = e.code,
            message = e.message,
        )
        return ResponseEntity.status(e.status).body(body)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = e.bindingResult.fieldErrors.firstOrNull()?.defaultMessage
            ?: CommonErrorCode.INVALID_INPUT.message
        val body = ErrorResponse(
            code = CommonErrorCode.INVALID_INPUT.errorCode,
            message = message,
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(e: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val message = e.constraintViolations.firstOrNull()?.message
            ?:  CommonErrorCode.INVALID_INPUT.message
        val body = ErrorResponse(
            code = CommonErrorCode.INVALID_INPUT.errorCode,
            message = message,
        )
        return ResponseEntity.status( CommonErrorCode.INVALID_INPUT.httpStatus).body(body)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(e: AccessDeniedException): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            code = CommonErrorCode.ACCESS_DENIED_ROLE.errorCode,
            message = CommonErrorCode.ACCESS_DENIED_ROLE.message,
        )
        return ResponseEntity.status(CommonErrorCode.ACCESS_DENIED_ROLE.httpStatus).body(body)
    }
}

