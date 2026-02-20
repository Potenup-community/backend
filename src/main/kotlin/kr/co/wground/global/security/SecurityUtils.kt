package kr.co.wground.global.security

import kr.co.wground.global.common.UserId
import kr.co.wground.global.jwt.UserPrincipal
import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtils {
    fun getCurrentUserId(): UserId? {
        val principal = SecurityContextHolder.getContext()
            .authentication?.principal as? UserPrincipal
        return principal?.userId
    }
}