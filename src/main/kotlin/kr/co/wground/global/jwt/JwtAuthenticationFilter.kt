package kr.co.wground.global.jwt

import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.global.jwt.constant.CSRF
import kr.co.wground.global.jwt.constant.HEADER_NAME
import kr.co.wground.global.jwt.constant.ROLE_PREFIX
import kr.co.wground.global.jwt.constant.SUBSTRING_INDEX
import kr.co.wground.global.jwt.constant.TOKEN_START
import kr.co.wground.global.jwt.constant.TokenType
import kr.co.wground.session.application.dto.DeviceContext
import kr.co.wground.user.application.common.LoginService
import kr.co.wground.user.application.exception.UserServiceErrorCode
import org.springframework.beans.factory.annotation.Value
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
    private val loginService: LoginService,
    @Value("\${jwt.expiration-ms}")
    private val accessExpirationMs: Long,
    @Value("\${jwt.refresh-expiration-ms}")
    private val refreshExpirationMs: Long,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val accessToken = resolveTokenFromHeader(request) ?: resolveTokenFromCookie(request, TokenType.ACCESS)

        if (accessToken == null) {
            val refreshToken = resolveTokenFromCookie(request, TokenType.REFRESH)

            if (refreshToken != null) {
                try {
                    rePublishAndAuthenticate(refreshToken, request, response)
                    filterChain.doFilter(request, response)
                    return
                } catch (ex: BusinessException) {
                    handlerExceptionResolver.resolveException(request, response, null, ex)
                    return
                }
            }

            filterChain.doFilter(request, response)
            return
        }

        try {
            val claims = jwtProvider.parseTokenClaims(accessToken, TokenType.ACCESS)
            setAuthentication(claims.userId, claims.role, claims.sessionId)
        } catch (ex: ExpiredJwtException) {
            try {
                val refreshToken = resolveTokenFromCookie(request, TokenType.REFRESH)
                    ?: throw BusinessException(UserServiceErrorCode.REFRESH_TOKEN_NOT_FOUND)
                rePublishAndAuthenticate(refreshToken, request, response)
            } catch (e: BusinessException) {
                handlerExceptionResolver.resolveException(request, response, null, e)
                return
            }
        } catch (ex: BusinessException) {
            handlerExceptionResolver.resolveException(request, response, null, ex)
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun rePublishAndAuthenticate(
        refreshToken: String,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val deviceContext = extractDeviceContext(request)
        val tokenResponse = loginService.refreshAccessToken(refreshToken, deviceContext)

        val accessCookie = createTokenCookie(tokenResponse.accessToken, TokenType.ACCESS, accessExpirationMs)
        val refreshCookie = createTokenCookie(tokenResponse.refreshToken, TokenType.REFRESH, refreshExpirationMs)

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString())
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString())

        setAuthentication(tokenResponse.userId, tokenResponse.userRole.name, tokenResponse.sessionId)
    }

    private fun setAuthentication(userId: UserId, role: String, sessionId: String? = null) {
        val principal = UserPrincipal(userId, role, sessionId)
        val authorities = listOf(SimpleGrantedAuthority("${ROLE_PREFIX}${role}"))
        val authentication = UsernamePasswordAuthenticationToken(principal, null, authorities)
        SecurityContextHolder.getContext().authentication = authentication
    }

    private fun extractDeviceContext(request: HttpServletRequest): DeviceContext {
        val deviceId = request.getHeader("X-Device-Id")
            ?: WebUtils.getCookie(request, "deviceId")?.value
            ?: "unknown"
        return DeviceContext(
            deviceId = deviceId,
            deviceName = request.getHeader("X-Device-Name"),
            userAgent = request.getHeader(HttpHeaders.USER_AGENT),
            ipAddress = request.remoteAddr,
        )
    }

    private fun createTokenCookie(token: String, tokenType: TokenType, expiredMs: Long): ResponseCookie =
        ResponseCookie.from(tokenType.tokenType, token)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(Duration.ofMillis(expiredMs))
            .sameSite(CSRF)
            .build()

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
        return if (cookie != null && cookie.value.isNotBlank()) cookie.value else null
    }
}
