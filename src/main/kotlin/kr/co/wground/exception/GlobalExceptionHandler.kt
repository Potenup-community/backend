package kr.co.wground.exception

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.InvalidNullException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import jakarta.validation.ConstraintViolationException
import kr.co.wground.comment.exception.CommentErrorCode
import kr.co.wground.global.common.response.ErrorResponse
import kr.co.wground.post.exception.PostErrorCode
import kr.co.wground.track.domain.exception.TrackDomainErrorCode
import kr.co.wground.user.application.exception.UserServiceErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse.of(e, emptyList())
        return ResponseEntity.status(e.httpStatus).body(body)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        e: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errors = e.bindingResult.fieldErrors
            .map { fieldError ->
                ErrorResponse.CustomError(
                    field = fieldError.field,
                    reason = fieldError.defaultMessage ?: "No message"
                )
            }

        val path = request.getDescription(false).removePrefix("uri=")
        val errorCode: ErrorCode = when {
            path.startsWith("/api/v1/posts") -> PostErrorCode.INVALID_POST_INPUT
            path.startsWith("/api/v1/comments") -> CommentErrorCode.INVALID_COMMENT_INPUT
            path.startsWith("/api/v1/users") -> UserServiceErrorCode.INVALID_USER_INPUT
            path.startsWith("/api/v1/admin/users") -> UserServiceErrorCode.INVALID_USER_INPUT
            path.startsWith("/api/v1/auth") -> UserServiceErrorCode.INVALID_USER_INPUT
            path.startsWith("/api/v1/admin/tracks") -> TrackDomainErrorCode.INVALID_TRACK_INPUT
            else -> CommonErrorCode.INVALID_INPUT
        }

        val body = ErrorResponse.of(errorCode, errors)
        return ResponseEntity.status(errorCode.httpStatus).body(body)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        val errorCode = CommonErrorCode.INVALID_INPUT
        val errors = mutableListOf<ErrorResponse.CustomError>()

        val fieldName = if (e.cause is MismatchedInputException) {
            (e.cause as MismatchedInputException).path
                .joinToString(separator = ".") { it.fieldName ?: "" }
        } else {
            "unknown"
        }

        val reason = when (val cause = e.cause) {
            is InvalidNullException -> "필수 값입니다. 누락되었거나 null일 수 없습니다."
            is InvalidFormatException -> "'${cause.value}'은(는) 유효한 값이 아닙니다."
            else -> "입력 형식이 올바르지 않습니다."
        }

        errors.add(ErrorResponse.CustomError(field = fieldName.ifEmpty { "unknown" }, reason = reason))

        val body = ErrorResponse.of(errorCode, errors)
        return ResponseEntity.status(errorCode.httpStatus).body(body)
    }

    @ExceptionHandler(
        MissingServletRequestParameterException::class,
        MethodArgumentTypeMismatchException::class,
        HttpRequestMethodNotSupportedException::class,
    )
    fun handleBadServletRequests(e: Exception): ResponseEntity<ErrorResponse> {
        val message = when (e) {
            is MissingServletRequestParameterException ->
                "필수 파라미터가 누락되었습니다: ${e.parameterName}"

            is MethodArgumentTypeMismatchException ->
                "'${e.value}'은(는) 올바른 요청 값이 아닙니다."

            is HttpRequestMethodNotSupportedException ->
                "지원하지 않는 HTTP 메서드입니다."

            else -> CommonErrorCode.INVALID_INPUT.message
        }

        val body = ErrorResponse(
            code = CommonErrorCode.INVALID_INPUT.code,
            message = message,
            errors = emptyList(),
        )
        val status = if (e is HttpRequestMethodNotSupportedException) {
            e.statusCode
        } else {
            CommonErrorCode.INVALID_INPUT.httpStatus
        }
        return ResponseEntity.status(status).body(body)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unhandled exception", e)
        val errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR
        val body = ErrorResponse.of(errorCode, emptyList())
        return ResponseEntity.status(errorCode.httpStatus).body(body)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(e: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val errors = e.constraintViolations.map { violation ->
            val field = violation.propertyPath
                .iterator()
                .asSequence()
                .mapNotNull { it.name }
                .lastOrNull()
                ?: "unknown"

            ErrorResponse.CustomError(
                field = field,
                reason = violation.message
            )
        }

        val body = ErrorResponse.of(CommonErrorCode.INVALID_INPUT, errors)
        return ResponseEntity.status(CommonErrorCode.INVALID_INPUT.httpStatus).body(body)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(e: AccessDeniedException): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            code = CommonErrorCode.ACCESS_DENIED_ROLE.code,
            message = CommonErrorCode.ACCESS_DENIED_ROLE.message,
        )
        return ResponseEntity.status(CommonErrorCode.ACCESS_DENIED_ROLE.httpStatus).body(body)
    }
}
