package kr.co.wground.user.presentation

import jakarta.validation.Valid
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.global.jwt.constant.CSRF
import kr.co.wground.user.application.common.LoginService
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.presentation.dto.TokenType
import kr.co.wground.user.presentation.request.LoginRequest
import kr.co.wground.user.presentation.response.TokenResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val memberService: LoginService,
    @Value("\${jwt.expiration-ms}")
    private val accessExpirationMs: Long,
    @Value("\${jwt.refresh-expiration-ms}")
    private val refreshExpirationMs: Long,
) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<TokenResponse> {
        val response = memberService.login(loginRequest)

        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, setCookie(response.accessToken, TokenType.ACCESS).toString())
            .header(HttpHeaders.SET_COOKIE, setCookie(response.refreshToken, TokenType.REFRESH).toString())
            .build()
    }

    @PostMapping("/refresh")
    fun refreshAccessToken(@CookieValue refreshToken: String?): ResponseEntity<TokenResponse> {
        if (refreshToken.isNullOrBlank()) {
            throw BusinessException(UserServiceErrorCode.REFRESH_TOKEN_NOT_FOUND)
        }

        val response = memberService.refreshAccessToken(refreshToken)

        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, setCookie(response.accessToken, TokenType.ACCESS).toString())
            .header(HttpHeaders.SET_COOKIE, setCookie(response.refreshToken, TokenType.REFRESH).toString())
            .build()
    }

    @DeleteMapping("/logout")
    fun logout(userId : CurrentUserId): ResponseEntity<Unit> {
        memberService.logout(userId.value)

        val expiredAccess = setCookie("", TokenType.ACCESS, 0)
        val expiredRefresh = setCookie("", TokenType.REFRESH, 0)

        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, expiredAccess.toString())
            .header(HttpHeaders.SET_COOKIE, expiredRefresh.toString())
            .build()
    }

    private fun setCookie(token: String, type: TokenType, maxAge: Long? = null): ResponseCookie {
        val duration = maxAge ?: when (type) {
            TokenType.ACCESS -> accessExpirationMs
            TokenType.REFRESH -> refreshExpirationMs
        }

        return ResponseCookie.from(type.tokenType, token)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(duration)
            .sameSite(CSRF)
            // .domain("www.depth.co.kr")
            .build()
    }
}
