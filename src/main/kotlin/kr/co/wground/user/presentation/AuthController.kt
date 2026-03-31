package kr.co.wground.user.presentation

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import kr.co.wground.global.jwt.UserPrincipal
import kr.co.wground.global.jwt.constant.CSRF
import kr.co.wground.global.jwt.constant.TokenType
import kr.co.wground.session.application.dto.DeviceContext
import kr.co.wground.user.application.common.LoginService
import kr.co.wground.user.presentation.request.LoginRequest
import kr.co.wground.user.presentation.response.AuthStatusResponse
import kr.co.wground.user.presentation.response.RoleResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.WebUtils
import java.time.Duration
import java.util.UUID

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val memberService: LoginService,
    @Value("\${jwt.expiration-ms}")
    private val accessExpirationMs: Long,
    @Value("\${jwt.refresh-expiration-ms}")
    private val refreshExpirationMs: Long,
) : AuthApi {

    @PostMapping("/login")
    override fun login(
        @Valid @RequestBody loginRequest: LoginRequest,
        request: HttpServletRequest,
    ): ResponseEntity<RoleResponse> {
        val deviceContext = extractDeviceContext(request)
        val response = memberService.login(loginRequest, deviceContext)

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, setCookie(response.accessToken, TokenType.ACCESS).toString())
            .header(HttpHeaders.SET_COOKIE, setCookie(response.refreshToken, TokenType.REFRESH).toString())
            .body(RoleResponse(response.role))
    }

    @DeleteMapping("/logout")
    override fun logout(authentication: Authentication): ResponseEntity<Unit> {
        val principal = authentication.principal as UserPrincipal
        memberService.logout(principal.userId, principal.sessionId ?: "")

        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, setCookie("", TokenType.ACCESS, 0).toString())
            .header(HttpHeaders.SET_COOKIE, setCookie("", TokenType.REFRESH, 0).toString())
            .build()
    }

    @GetMapping("/me")
    override fun getAuthStatus(authentication: Authentication): ResponseEntity<AuthStatusResponse> {
        val principal = authentication.principal as UserPrincipal

        return ResponseEntity.ok(
            AuthStatusResponse(
                isAuthenticated = true,
                userId = principal.userId,
                role = principal.role,
            )
        )
    }

    private fun extractDeviceContext(request: HttpServletRequest): DeviceContext {
        val deviceId = request.getHeader("X-Device-Id")
            ?: WebUtils.getCookie(request, "deviceId")?.value
            ?: UUID.randomUUID().toString()
        return DeviceContext(
            deviceId = deviceId,
            deviceName = request.getHeader("X-Device-Name"),
            userAgent = request.getHeader(HttpHeaders.USER_AGENT),
            ipAddress = request.remoteAddr,
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
            .build()
    }
}
