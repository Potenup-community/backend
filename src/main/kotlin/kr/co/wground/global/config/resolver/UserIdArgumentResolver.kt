package kr.co.wground.global.config.resolver

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.security.SecurityUtils
import kr.co.wground.user.application.exception.UserServiceErrorCode
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class UserIdArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType == CurrentUserId::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val userId = SecurityUtils.getCurrentUserId()
            ?: throw BusinessException(UserServiceErrorCode.AUTHENTICATION_NOT_FOUND)
        return CurrentUserId(userId)
    }
}