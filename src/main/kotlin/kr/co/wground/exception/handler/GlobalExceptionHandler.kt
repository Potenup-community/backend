package kr.co.wground.exception.handler

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.InvalidNullException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import kr.co.wground.comment.exception.CommentErrorCode
import kr.co.wground.exception.BusinessException
import kr.co.wground.exception.CommonErrorCode
import kr.co.wground.exception.ErrorCode
import kr.co.wground.global.common.response.ErrorResponse
import kr.co.wground.global.monitoring.MonitoringConstants
import kr.co.wground.post.exception.PostErrorCode
import kr.co.wground.track.domain.exception.TrackDomainErrorCode
import kr.co.wground.user.application.exception.UserServiceErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.MaxUploadSizeExceededException

@RestControllerAdvice
class GlobalExceptionHandler {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(request: HttpServletRequest, e: BusinessException): ResponseEntity<ErrorResponse> {
        log.debug("비지니스 예외 처리 발생\n status: {}\ncode: {}\nmessage: {}\$", e.httpStatus, e.code, e.message)
        request.setAttribute(MonitoringConstants.EXCEPTION_FOR_LOG, e)
        request.setAttribute(MonitoringConstants.ERROR_CODE_FOR_LOG, e.code)
        request.setAttribute(MonitoringConstants.ERROR_MESSAGE_FOR_LOG, e.message)

        val body = ErrorResponse.of(e)
        return ResponseEntity.status(e.httpStatus).body(body)
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSizeExceededException(
        request: HttpServletRequest,
        e: MaxUploadSizeExceededException
    ): ResponseEntity<ErrorResponse> {
        val errorCode = CommonErrorCode.MAX_UPLOAD_SIZE_EXCEEDED
        val additionalInfo = "허용된 최대 파일 크기 = " + e.maxUploadSize
        val errorMessage = CommonErrorCode.MAX_UPLOAD_SIZE_EXCEEDED.message + ", " + additionalInfo
        log.debug("업로드 용량 초과 발생: ${e.message}")
        request.setAttribute(MonitoringConstants.EXCEPTION_FOR_LOG, e)
        request.setAttribute(MonitoringConstants.ERROR_CODE_FOR_LOG, errorCode.code)
        request.setAttribute(MonitoringConstants.ERROR_MESSAGE_FOR_LOG, errorMessage)

        val body = ErrorResponse.of(errorCode = errorCode, additionalInfo = additionalInfo)
        return ResponseEntity.status(errorCode.httpStatus).body(body)
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

        val errorMessage: String = errorCode.message + ", " + errors
            .groupBy { it.field }
            .entries
            .sortedBy { it.key }
            .joinToString("; ") { (field, errs) ->
                "$field: ${errs.map { it.reason }.distinct().joinToString(", ")}"
            }
        request.setAttribute(MonitoringConstants.EXCEPTION_FOR_LOG, e, WebRequest.SCOPE_REQUEST)
        request.setAttribute(MonitoringConstants.ERROR_CODE_FOR_LOG, errorCode.code, WebRequest.SCOPE_REQUEST)
        request.setAttribute(MonitoringConstants.ERROR_MESSAGE_FOR_LOG, errorMessage, WebRequest.SCOPE_REQUEST)

        val body = ErrorResponse.of(errorCode, errors)

        log.debug("RequestBody Valid 오류 발생\ncode: ${body.code}\nmessage: ${body.message}")
        return ResponseEntity.status(errorCode.httpStatus).body(body)
    }

    @ExceptionHandler(HandlerMethodValidationException::class)
    fun handleHandlerMethodValidationException(
        request: HttpServletRequest,
        e: HandlerMethodValidationException
    ): ResponseEntity<ErrorResponse> {
        val errorCode = CommonErrorCode.INVALID_INPUT
        val errors = e.parameterValidationResults.map { result ->

            val fieldName: String = result.methodParameter.parameterName ?: "unknown"
            val reason = result.resolvableErrors
                .joinToString(", ") { err ->
                    err.defaultMessage
                        ?: err.codes?.firstOrNull()
                        ?: "요청 파라미터 검증 실패"
                }

            ErrorResponse.CustomError(fieldName, reason)
        }

        val errorMessage: String = errorCode.message + ", " + errors
            .groupBy { it.field }
            .entries
            .sortedBy { it.key }
            .joinToString(separator = "; ") { (field, errs) ->
                val reasons = errs
                    .map { it.reason }
                    .distinct()
                    .joinToString(", ")
                "$field: $reasons"
            }
        request.setAttribute(MonitoringConstants.EXCEPTION_FOR_LOG, e)
        request.setAttribute(MonitoringConstants.ERROR_CODE_FOR_LOG, errorCode.code)
        request.setAttribute(MonitoringConstants.ERROR_MESSAGE_FOR_LOG, errorMessage)

        val body = ErrorResponse.of(errorCode, errors)

        log.debug("PathVariable, RequestParam 오류 발생\ncode: ${body.code}\nmessage: ${body.message}")
        return ResponseEntity.status(errorCode.httpStatus).body(body)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        request: HttpServletRequest,
        e: HttpMessageNotReadableException
    ): ResponseEntity<ErrorResponse> {
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

        val customError = ErrorResponse.CustomError(field = fieldName.ifEmpty { "unknown" }, reason = reason)
        errors.add(customError)

        val body = ErrorResponse.of(errorCode, errors)

        val errorMessage = "${errorCode.message}, field = ${customError.field}, reason = ${customError.reason}"
        request.setAttribute(MonitoringConstants.EXCEPTION_FOR_LOG, e)
        request.setAttribute(MonitoringConstants.ERROR_CODE_FOR_LOG, errorCode.code)
        request.setAttribute(MonitoringConstants.ERROR_MESSAGE_FOR_LOG, errorMessage)


        log.debug("요청 본문 파싱 오류 발생\ncode: ${body.code}\nerrorMessage: $errorMessage")
        return ResponseEntity.status(errorCode.httpStatus).body(body)
    }

    @ExceptionHandler(
        MissingServletRequestParameterException::class,
        MethodArgumentTypeMismatchException::class,
        HttpRequestMethodNotSupportedException::class,
    )
    fun handleBadServletRequests(request: HttpServletRequest, e: Exception): ResponseEntity<ErrorResponse> {
        val message = when (e) {
            is MissingServletRequestParameterException -> {
                "필수 파라미터가 누락되었습니다: ${e.parameterName}"
                log.debug("필수 파라미터가 누락되었습니다: ${e.parameterName}")
            }

            is MethodArgumentTypeMismatchException -> {
                "'${e.value}'은(는) 올바른 요청 값이 아닙니다."
                log.debug("'{}'은(는) 올바른 요청 값이 아닙니다.", e.value)
            }

            is HttpRequestMethodNotSupportedException -> {
                "지원하지 않는 HTTP 메서드입니다."
                log.debug("지원하지 않는 HTTP 메서드입니다.")
            }

            else -> CommonErrorCode.INVALID_INPUT.message
        }

        val status = if (e is HttpRequestMethodNotSupportedException) {
            e.statusCode
        } else {
            CommonErrorCode.INVALID_INPUT.httpStatus
        }

        val errorMessage = "${CommonErrorCode.INVALID_INPUT.message}, ${message}"
        request.setAttribute(MonitoringConstants.EXCEPTION_FOR_LOG, e)
        request.setAttribute(MonitoringConstants.ERROR_CODE_FOR_LOG, CommonErrorCode.INVALID_INPUT.code)
        request.setAttribute(MonitoringConstants.ERROR_MESSAGE_FOR_LOG, errorMessage)

        val body = ErrorResponse.of(CommonErrorCode.INVALID_INPUT)
        return ResponseEntity.status(status).body(body)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(request: HttpServletRequest, e: Exception): ResponseEntity<ErrorResponse> {

        request.setAttribute(MonitoringConstants.EXCEPTION_FOR_LOG, e)
        request.setAttribute(MonitoringConstants.ERROR_CODE_FOR_LOG, CommonErrorCode.INTERNAL_SERVER_ERROR.code)
        request.setAttribute(MonitoringConstants.ERROR_MESSAGE_FOR_LOG, CommonErrorCode.INTERNAL_SERVER_ERROR.message)

        val body = ErrorResponse.of(CommonErrorCode.INTERNAL_SERVER_ERROR)

        log.debug("예상치 못한 예외가 발생했습니다\nrootCause: ${getRootCauseMessage(e)}\nmessage: ${e.message}")
        return ResponseEntity.status(CommonErrorCode.INTERNAL_SERVER_ERROR.httpStatus).body(body)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        request: HttpServletRequest,
        e: ConstraintViolationException
    ): ResponseEntity<ErrorResponse> {
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

        val errorMessages: String = CommonErrorCode.INVALID_INPUT.message + ", " + errors
            .groupBy { it.field }
            .entries
            .sortedBy { it.key }
            .joinToString("; ") { (field, errs) ->
                "$field: ${errs.map { it.reason }.distinct().joinToString(", ")}"
            }
        request.setAttribute(MonitoringConstants.EXCEPTION_FOR_LOG, e)
        request.setAttribute(MonitoringConstants.ERROR_CODE_FOR_LOG, CommonErrorCode.INVALID_INPUT.code)
        request.setAttribute(MonitoringConstants.ERROR_MESSAGE_FOR_LOG, errorMessages)

        val body = ErrorResponse.of(CommonErrorCode.INVALID_INPUT, errors)

        log.debug("매서드 파라미터 검증 실패\nrootCause: ${getRootCauseMessage(e)}\nmessage: $errorMessages")
        return ResponseEntity.status(CommonErrorCode.INVALID_INPUT.httpStatus).body(body)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(request: HttpServletRequest, e: AccessDeniedException): ResponseEntity<ErrorResponse> {

        request.setAttribute(MonitoringConstants.EXCEPTION_FOR_LOG, e)
        request.setAttribute(MonitoringConstants.ERROR_CODE_FOR_LOG, CommonErrorCode.ACCESS_DENIED_ROLE.code)
        request.setAttribute(MonitoringConstants.ERROR_MESSAGE_FOR_LOG, CommonErrorCode.ACCESS_DENIED_ROLE.message)

        val body = ErrorResponse.of(CommonErrorCode.ACCESS_DENIED_ROLE)

        log.debug("접근제한 오류 발생\nrootCause: ${getRootCauseMessage(e)}\nmessage: ${e.message}")
        return ResponseEntity.status(CommonErrorCode.ACCESS_DENIED_ROLE.httpStatus).body(body)
    }

    @ExceptionHandler(AuthorizationDeniedException::class)
    fun handleAuthorizationDenied(
        request: HttpServletRequest,
        e: AuthorizationDeniedException
    ): ResponseEntity<ErrorResponse> {

        request.setAttribute(MonitoringConstants.EXCEPTION_FOR_LOG, e)
        request.setAttribute(MonitoringConstants.ERROR_CODE_FOR_LOG, CommonErrorCode.ACCESS_DENIED_ROLE.code)
        request.setAttribute(MonitoringConstants.ERROR_MESSAGE_FOR_LOG, CommonErrorCode.ACCESS_DENIED_ROLE.message)

        val body = ErrorResponse.of(CommonErrorCode.ACCESS_DENIED_ROLE)

        log.debug("접근제한 권한 오류 발생\nmessage: {}\n권한거절 사유: {}", e.message, e.authorizationResult)
        return ResponseEntity.status(CommonErrorCode.ACCESS_DENIED_ROLE.httpStatus).body(body)
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(
        request: HttpServletRequest,
        e: AuthenticationException
    ): ResponseEntity<ErrorResponse> {

        request.setAttribute(MonitoringConstants.EXCEPTION_FOR_LOG, e)
        request.setAttribute(MonitoringConstants.ERROR_CODE_FOR_LOG, CommonErrorCode.AUTHORIZATION_FAILURE.code)
        request.setAttribute(MonitoringConstants.ERROR_MESSAGE_FOR_LOG, CommonErrorCode.AUTHORIZATION_FAILURE.message)

        val body = ErrorResponse.of(CommonErrorCode.AUTHORIZATION_FAILURE)

        log.debug(
            "인증 실패 오류 발생\ncause: {}\nmessage: {}\nauthentication: {}",
            getRootCauseMessage(e),
            e.message,
            e.authenticationRequest
        )
        return ResponseEntity.status(CommonErrorCode.AUTHORIZATION_FAILURE.httpStatus).body(body)
    }

    private fun getRootCauseMessage(e: Throwable): String {
        var cause: Throwable? = e
        while (cause?.cause != null) {
            cause = cause.cause
        }
        return cause?.message ?: "알 수 없는 에러"
    }
}