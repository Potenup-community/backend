package kr.co.wground.user.presentation

import jakarta.validation.Valid
import kr.co.wground.user.application.common.LoginService
import kr.co.wground.user.presentation.request.LoginRequest
import kr.co.wground.user.presentation.request.RefreshTokenRequest
import kr.co.wground.user.presentation.response.AccessTokenResponse
import kr.co.wground.user.presentation.response.LoginResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(private val memberService: LoginService) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<LoginResponse> {
        val response = memberService.login(loginRequest)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    fun refreshAccessToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<AccessTokenResponse> {
        val response = memberService.refreshAccessToken(request)
        return ResponseEntity.ok(response)
    }
}
