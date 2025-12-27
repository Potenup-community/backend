package kr.co.wground.user.presentation

import jakarta.validation.Valid
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.global.jwt.constant.CSRF
import kr.co.wground.global.jwt.constant.TokenType
import kr.co.wground.user.application.common.LoginService
import kr.co.wground.user.presentation.request.LoginRequest
import kr.co.wground.user.presentation.response.RoleResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import kr.co.wground.global.jwt.UserPrincipal
import kr.co.wground.user.presentation.response.AuthStatusResponse
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping

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
    fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<RoleResponse> {
        val response = memberService.login(loginRequest)

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, setCookie(response.accessToken, TokenType.ACCESS).toString())
            .header(HttpHeaders.SET_COOKIE, setCookie(response.refreshToken, TokenType.REFRESH).toString())
            .body(RoleResponse(response.role))
    }

    @DeleteMapping("/logout")
    fun logout(userId: CurrentUserId): ResponseEntity<Unit> {
        memberService.logout(userId.value)

        val expiredAccess = setCookie("", TokenType.ACCESS, 0)
        val expiredRefresh = setCookie("", TokenType.REFRESH, 0)

        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, expiredAccess.toString())
            .header(HttpHeaders.SET_COOKIE, expiredRefresh.toString())
            .build()
    }

    @GetMapping("/me")
    fun getAuthStatus(
        authentication: Authentication,
    ): ResponseEntity<AuthStatusResponse> {
        val principal = authentication.principal as UserPrincipal

        return ResponseEntity.ok(
            AuthStatusResponse(
                isAuthenticated = true,
                userId = principal.userId,
                role = principal.role
            )
        )
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
            .maxAge(Duration.ofMillis(duration))
            .sameSite(CSRF)
            // .domain("www.depth.co.kr")
            .build()
    }
}
