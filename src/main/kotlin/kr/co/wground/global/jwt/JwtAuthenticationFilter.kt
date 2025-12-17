package kr.co.wground.global.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.jwt.constant.CSRF
import kr.co.wground.global.jwt.constant.HEADER_NAME
import kr.co.wground.global.jwt.constant.SUBSTRING_INDEX
import kr.co.wground.global.jwt.constant.TOKEN_START
import kr.co.wground.global.jwt.constant.TokenType
import kr.co.wground.user.application.common.LoginService
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.infra.UserRepository
import kr.co.wground.user.presentation.response.TokenResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver
import org.springframework.web.util.WebUtils
import java.time.Duration

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val handlerExceptionResolver: HandlerExceptionResolver,
    private val userRepository: UserRepository,
    private val loginService: LoginService,
    @Value("\${jwt.expiration-ms}")
    private val accessExpirationMs: Long,
    @Value("\${jwt.refresh-expiration-ms}")
    private val refreshExpirationMs: Long,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = resolveTokenFromHeader(request) ?: resolveTokenFromCookie(request, TokenType.ACCESS)

        if (token == null) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val userId = jwtProvider.validateAccessToken(token)
            setAuthentication(userId)

        } catch (ex: BusinessException) {
            if (ex.code == UserServiceErrorCode.TOKEN_EXPIRED.code) {
                try {
                    val refreshToken = resolveTokenFromCookie(request, TokenType.REFRESH) ?: throw ex

                    val tokenResponse = loginService.refreshAccessToken(refreshToken)

                    val accessCookie =
                        createTokenCookie(tokenResponse.accessToken, TokenType.ACCESS, accessExpirationMs)
                    val refreshCookie =
                        createTokenCookie(tokenResponse.refreshToken, TokenType.REFRESH, refreshExpirationMs)

                    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString())
                    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString())

                    val userId = jwtProvider.validateAccessToken(tokenResponse.accessToken)
                    setAuthentication(userId)

                    filterChain.doFilter(request, response)
                    return
                } catch (e: BusinessException) {
                    handlerExceptionResolver.resolveException(request, response, null, e)
                    return
                }
            }
            handlerExceptionResolver.resolveException(request, response, null, ex)
            return
        }
        filterChain.doFilter(request, response)
    }

    private fun setAuthentication(userId: Long) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

        val principle = UserPrincipal(userId)
        val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
        val authentication = UsernamePasswordAuthenticationToken(principle, null, authorities)

        SecurityContextHolder.getContext().authentication = authentication
    }

    private fun createTokenCookie(token: String, tokenType: TokenType, expiredMs: Long): ResponseCookie {
        return ResponseCookie.from(tokenType.tokenType, token)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(Duration.ofMillis(expiredMs))
            .sameSite(CSRF)
            .build()
    }

    private fun resolveTokenFromHeader(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(HEADER_NAME)
        return if (bearerToken != null && bearerToken.startsWith(TOKEN_START)) {
            bearerToken.substring(SUBSTRING_INDEX)
        } else {
            null
        }
    }

    private fun resolveTokenFromCookie(request: HttpServletRequest, tokenType: TokenType): String? {
        val cookie = WebUtils.getCookie(request, tokenType.tokenType)
        return if (cookie != null && cookie.value.isNotBlank()) {
            cookie.value
        } else {
            null
        }
    }
}
