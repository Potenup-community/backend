package kr.co.wground.user.presentation

import kr.co.wground.global.config.resolver.UserId
import kr.co.wground.user.application.login.LoginService
import kr.co.wground.user.presentation.request.LoginRequest
import kr.co.wground.user.presentation.request.RefreshTokenRequest
import kr.co.wground.user.presentation.response.AccessTokenResponse
import kr.co.wground.user.presentation.response.LoginResponse
import kr.co.wground.user.presentation.response.UserInfoResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(private val memberService: LoginService) {

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<LoginResponse> {
        val response = memberService.login(loginRequest)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    fun refreshAccessToken(@RequestBody request: RefreshTokenRequest): ResponseEntity<AccessTokenResponse> {
        val response = memberService.refreshAccessToken(request)
        return ResponseEntity.ok(response)
    }
    //테스트용 정보 조회
    @GetMapping("/info")
    fun userInfo(userId : UserId): ResponseEntity<UserInfoResponse> {
        println("qweqweqwe: ${userId}")
        val response = memberService.userInfo(userId.value)
        return ResponseEntity.ok(response)
    }
}
