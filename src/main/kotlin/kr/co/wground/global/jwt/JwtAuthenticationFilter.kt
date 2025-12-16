package kr.co.wground.global.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.response.ErrorResponse
import kr.co.wground.global.jwt.constant.CSRF
import kr.co.wground.global.jwt.constant.HEADER_NAME
import kr.co.wground.global.jwt.constant.SUBSTRING_INDEX
import kr.co.wground.global.jwt.constant.TOKEN_START
import kr.co.wground.global.jwt.constant.TokenType
import kr.co.wground.user.application.common.LoginService
import kr.co.wground.user.infra.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseCookie
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.WebUtils
import java.time.Duration

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val userRepository: UserRepository,
    private val objectMapper: ObjectMapper,
    private val loginService: LoginService,
    @Value("\${jwt.expiration-ms}")
    private val accessExpirationMs: Long,
    @Value("\${jwt.refresh-expiration-ms}")
    private val refreshExpirationMs: Long,
) : OncePerRequestFilter() {
    private companion object {
        private const val TOKEN_EXPIRED = "U-0010"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = resolveTokenFromHeader(request) ?: resolveTokenFromCookie(request)

        if (token == null) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val userId = jwtProvider.validateAccessToken(token)
            setAuthentication(userId)

        } catch (ex: BusinessException) {
            if (ex.code == TOKEN_EXPIRED) {
                try {
                    val refreshToken = resolveRefreshTokenFromCookie(request) ?: throw ex

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
                    setErrorResponse(response, ex)
                    return
                }
            }
            setErrorResponse(response, ex)
            return
        }
        filterChain.doFilter(request, response)
    }

    private fun setAuthentication(userId: Long) {
        val user = userRepository.findByIdOrNull(userId)
        if (user != null) {
            val principle = UserPrincipal(userId)
            val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
            val authentication = UsernamePasswordAuthenticationToken(principle, null, authorities)
            SecurityContextHolder.getContext().authentication = authentication
        }
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

    private fun resolveTokenFromCookie(request: HttpServletRequest): String? {
        val cookie = WebUtils.getCookie(request, TokenType.ACCESS.tokenType)
        return if (cookie != null && cookie.value.isNotBlank()) {
            cookie.value
        } else {
            null
        }
    }

    private fun resolveRefreshTokenFromCookie(request: HttpServletRequest): String? {
        val cookie = WebUtils.getCookie(request, TokenType.REFRESH.tokenType)
        return if (cookie != null && cookie.value.isNotBlank()) {
            cookie.value
        } else {
            null
        }
    }

    private fun setErrorResponse(
        response: HttpServletResponse,
        e: BusinessException
    ) {
        response.status = e.httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.writer.write(objectMapper.writeValueAsString(ErrorResponse.of(e, emptyList())))
    }
}